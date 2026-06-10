package model;

import model.account.Account;
import model.engine.GameSession;

public class App {
    private MenuState menuState;
    private Account account;
    private GameSession gameSession;

    public App(MenuState menuState) {
        this.menuState = menuState;
    }

    public MenuState getMenuState() {
        return menuState;
    }

    public void setMenuState(MenuState menuState) {
        this.menuState = menuState;
    }

    public void login(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public GameSession getGameSession() {
        return gameSession;
    }

    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
    }
}
