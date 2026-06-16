package com.ussr.pvz.model;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;

import java.util.ArrayList;
import java.util.List;

public class App {
    private static MenuState menuState = MenuState.REGISTER;
    private static Account account;
    private static GameSession gameSession;
    private static List<Account> accounts = new ArrayList<>();

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
}
