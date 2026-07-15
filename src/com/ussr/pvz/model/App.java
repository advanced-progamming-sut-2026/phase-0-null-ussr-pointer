package com.ussr.pvz.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Collection;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.model.level.LevelManager;
import com.ussr.pvz.model.shop.ShopManager;
import com.ussr.pvz.service.SaveService;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {
    private static MenuState menuState = MenuState.REGISTER;
    private static Account account;
    private static GameSession gameSession;
    private static List<Map<String, Object>> cachedPlantsData = null;
    private static ShopManager shopManager;
    private static final LevelManager levelManager = new LevelManager();
    private static final List<Account> accounts = new ArrayList<>(
            SaveService.loadAccounts().stream()
                    .map(state -> new Account
                            (state, new Collection(new ArrayList<>(), new ArrayList<>()))).toList());

    static {
        loadPlantsDataToMemory();
    }

    public static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            List<Account> accounts = getAccounts();
            if (!accounts.isEmpty()) {
                List<AccountState> states = accounts.stream()
                        .map(Account::toState)
                        .toList();
                SaveService.saveAccounts(states);
            }
        }));
    }

    public static void loadPlantsDataToMemory() {
        if (cachedPlantsData != null) return;
        Gson gson = new GsonBuilder()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create();

        File allPlantsFile = new File("src/resources/plants.json");

        if (!allPlantsFile.exists()) {
            System.err.println("Critical Error: plants.json not found during App boot!");
            cachedPlantsData = new ArrayList<>();
            return;
        }

        try (FileReader reader = new FileReader(allPlantsFile)) {
            Type complexListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            cachedPlantsData = gson.fromJson(reader, complexListType);

            if (cachedPlantsData == null) {
                cachedPlantsData = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Error caching plants.json to memory: " + e.getMessage());
            cachedPlantsData = new ArrayList<>();
        }
    }

    public static LevelManager getLevelManager() {
        return levelManager;
    }

    public static List<Account> getAccounts() {
        return accounts;
    }

    public static void addAccount(Account account) {
        accounts.add(account);
    }

    public static MenuState getMenuState() {
        return App.menuState;
    }

    public static void setMenuState(MenuState menuState) {
        App.menuState = menuState;
    }

    public static void login(Account account) {
        App.account = account;
    }

    public static Account getAccount() {
        return App.account;
    }

    public static GameSession getGameSession() {
        return App.gameSession;
    }

    public static void setGameSession(GameSession gameSession) {
        App.gameSession = gameSession;
    }

    public static ShopManager getShopManager() {
        return shopManager;
    }

    public static void setShopManager(ShopManager shopManager) {
        App.shopManager = shopManager;
    }

    public static void initShop(){
        App.shopManager = new ShopManager();
    }

    public static List<Map<String, Object>> getCachedPlantsData() {
        return cachedPlantsData;
    }
}
