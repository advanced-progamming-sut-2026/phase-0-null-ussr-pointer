package com.ussr.pvz.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.LevelManager;
import com.ussr.pvz.model.shop.ShopManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {

    private static MenuState menuState =
            MenuState.REGISTER;

    /*
     * Client-side cache of the currently logged-in account.
     *
     * The server is the real owner of account data.
     */
    private static Account account;

    private static GameSession gameSession;

    /*
     * Set right before leaving the GAME screen for the Settings menu from the
     * pause overlay, so the freshly-rebuilt gameplay screen can re-open the
     * pause overlay on return instead of resuming unpaused. Consumed (read
     * once, then cleared) by GameplayController on construction.
     */
    private static boolean resumeToPauseMenu = false;

    private static List<Map<String, Object>>
            cachedPlantsData = null;

    private static ShopManager shopManager;

    private static boolean cheatedLevel = false;

    private static final LevelManager levelManager =
            new LevelManager();


    static {
        loadPlantsDataToMemory();
    }


    // =========================================================
    // DEBUG
    // =========================================================

    private static boolean debugModeEnabled =
            false;

    private static boolean gridEnabled =
            false;


    public static boolean isDebugModeEnabled() {
        return debugModeEnabled;
    }


    public static void setDebugModeEnabled(
            boolean value
    ) {
        debugModeEnabled = value;
    }


    public static boolean isGridEnabled() {
        return gridEnabled;
    }


    public static void setGridEnabled(
            boolean value
    ) {
        gridEnabled = value;
    }


    // =========================================================
    // GAME SESSION
    // =========================================================

    public static void setGameSession(
            GameSession gameSession
    ) {

        App.gameSession =
                gameSession;

        if (gameSession == null) {

            gridEnabled =
                    false;

            resumeToPauseMenu =
                    false;
        }
    }


    public static GameSession getGameSession() {
        return App.gameSession;
    }


    public static void setResumeToPauseMenu(
            boolean value
    ) {
        resumeToPauseMenu = value;
    }

    public static boolean consumeResumeToPauseMenu() {
        boolean value = resumeToPauseMenu;
        resumeToPauseMenu = false;
        return value;
    }


    // =========================================================
    // ACCOUNT
    // =========================================================

    public static void login(
            Account account
    ) {

        App.account =
                account;
    }


    public static void logout() {

        App.account =
                null;
    }


    public static Account getAccount() {
        return App.account;
    }


    // =========================================================
    // SERVER PERSISTENCE
    // =========================================================

    /*
     * Kept temporarily so existing startup code does not break.
     *
     * Account persistence is now handled by the server,
     * therefore the client has nothing to save on shutdown.
     */
    public static void registerShutdownHook() {

        // Intentionally empty.
        // Server owns account persistence.
    }


    // =========================================================
    // PLANT DATA
    // =========================================================

    public static void loadPlantsDataToMemory() {

        if (cachedPlantsData != null) {
            return;
        }

        Gson gson =
                new GsonBuilder()
                        .setObjectToNumberStrategy(
                                ToNumberPolicy.LONG_OR_DOUBLE
                        )
                        .create();

        File allPlantsFile =
                new File(
                        "src/resources/plants.json"
                );

        if (!allPlantsFile.exists()) {

            System.err.println(
                    "Critical Error: plants.json "
                            + "not found during App boot!"
            );

            cachedPlantsData =
                    new ArrayList<>();

            return;
        }

        try (FileReader reader =
                     new FileReader(
                             allPlantsFile
                     )) {

            Type complexListType =
                    new TypeToken<
                            List<Map<String, Object>>
                            >() {
                    }.getType();

            cachedPlantsData =
                    gson.fromJson(
                            reader,
                            complexListType
                    );

            if (cachedPlantsData == null) {

                cachedPlantsData =
                        new ArrayList<>();
            }

        } catch (IOException e) {

            System.err.println(
                    "Error caching plants.json "
                            + "to memory: "
                            + e.getMessage()
            );

            cachedPlantsData =
                    new ArrayList<>();
        }
    }


    public static List<Map<String, Object>>
    getCachedPlantsData() {

        return cachedPlantsData;
    }


    // =========================================================
    // MENU
    // =========================================================

    public static MenuState getMenuState() {

        return App.menuState;
    }


    public static void setMenuState(
            MenuState menuState
    ) {

        App.menuState =
                menuState;
    }


    // =========================================================
    // LEVEL
    // =========================================================

    public static LevelManager getLevelManager() {

        return levelManager;
    }


    // =========================================================
    // SHOP
    // =========================================================

    public static ShopManager getShopManager() {

        return shopManager;
    }


    public static void setShopManager(
            ShopManager shopManager
    ) {

        App.shopManager =
                shopManager;
    }


    public static void initShop() {

        App.shopManager =
                new ShopManager();
    }


    // =========================================================
    // CHEAT STATE
    // =========================================================

    public static boolean isCheatedLevel() {

        return cheatedLevel;
    }


    public static void setCheatedLevel(
            boolean cheatedLevel
    ) {

        App.cheatedLevel =
                cheatedLevel;
    }
}