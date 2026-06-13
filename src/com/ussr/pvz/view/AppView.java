package com.ussr.pvz.view;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;

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
