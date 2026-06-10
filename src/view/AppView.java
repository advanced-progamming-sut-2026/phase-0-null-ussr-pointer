package view;

import model.App;
import model.MenuState;
import model.engine.GameSession;

import java.util.Scanner;

public class AppView {
    private AppMenu currentMenu;
    private App app;

    public AppView() {
        app = new App(MenuState.MAIN);
    }

    public void run(Scanner scanner) {
    }

    public void setCurrentMenu(MenuState menuState) {
    }

    public void exit() {
        currentMenu = null;
    }
}
