package com.ussr.pvz.view;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.view.mainmenu.*;
import com.ussr.pvz.view.mainmenu.gamemenu.ChoosePlantMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.CollectionMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.GameMenu;

import java.util.Scanner;

public class AppView {
    private AppMenu currentMenu;
    private App app;

    public AppView() {
        app = new App(MenuState.MAIN);
    }

    public void run(Scanner scanner) {
        while (true) {
            setCurrentMenu(app.getMenuState());
            if (currentMenu != null) {
                currentMenu.run(scanner);
            } else
                break;
        }
    }

    public void setCurrentMenu(MenuState menuState) {
        switch (menuState) {
            case MAIN:
                currentMenu = new MainMenu();
                break;
            case REGISTER:
                currentMenu = new RegisterMenu();
                break;
            case LOGIN:
                currentMenu = new LoginMenu();
                break;
            case GAME:
                currentMenu = new GameMenu();
                break;
            case NEWS:
                currentMenu = new NewsMenu();
                break;
            case NETWORK:
                currentMenu = new NetworkMenu();
                break;
            case PROFILE:
                currentMenu = new ProfileMenu();
                break;
            case SETTING:
                currentMenu = new SettingMenu();
                break;
            case COLLECTION:
                currentMenu = new CollectionMenu();
                break;
            case GREENHOUSE:
                currentMenu = new GreenHouse();
                break;
            case TRAVEL_LOG:
                currentMenu = new TravelLogMenu();
                break;
            case LEADERBOARD:
                currentMenu = new LeaderBoardMenu();
                break;
            case CHOOSE_PLANT:
                currentMenu = new ChoosePlantMenu();
                break;
            case null, default:
                currentMenu = null;
        }
    }

    public void exit() {
        currentMenu = null;
    }
}
