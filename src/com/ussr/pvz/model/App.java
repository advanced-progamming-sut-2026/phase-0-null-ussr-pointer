package com.ussr.pvz.model;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;

public class App {
    private static MenuState menuState = MenuState.REGISTER;
    private static Account account;
    private static GameSession gameSession;

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
