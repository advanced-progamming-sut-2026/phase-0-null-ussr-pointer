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
    private final App app;

    private final MainMenu mainMenu;
    private final RegisterMenu registerMenu;
    private final LoginMenu loginMenu;
    private final GameMenu gameMenu;
    private final NewsMenu newsMenu;
    private final NetworkMenu networkMenu;
    private final ProfileMenu profileMenu;
    private final SettingMenu settingMenu;
    private final CollectionMenu collectionMenu;
    private final GreenHouse greenHouse;
    private final TravelLogMenu travelLogMenu;
    private final LeaderBoardMenu leaderBoardMenu;
    private final ChoosePlantMenu choosePlantMenu;

    public AppView() {
        app = new App();

        // Initialize all menu instances
        mainMenu = new MainMenu();
        registerMenu = new RegisterMenu();
        loginMenu = new LoginMenu();
        gameMenu = new GameMenu();
        newsMenu = new NewsMenu();
        networkMenu = new NetworkMenu();
        profileMenu = new ProfileMenu();
        settingMenu = new SettingMenu();
        collectionMenu = new CollectionMenu();
        greenHouse = new GreenHouse();
        travelLogMenu = new TravelLogMenu();
        leaderBoardMenu = new LeaderBoardMenu();
        choosePlantMenu = new ChoosePlantMenu();
    }

    public void run(Scanner scanner) {
        while (scanner.hasNextLine()) {
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
                currentMenu = mainMenu;
                break;
            case REGISTER:
                currentMenu = registerMenu;
                break;
            case LOGIN:
                currentMenu = loginMenu;
                break;
            case GAME:
                currentMenu = gameMenu;
                break;
            case NEWS:
                currentMenu = newsMenu;
                break;
            case NETWORK:
                currentMenu = networkMenu;
                break;
            case PROFILE:
                currentMenu = profileMenu;
                break;
            case SETTING:
                currentMenu = settingMenu;
                break;
            case COLLECTION:
                currentMenu = collectionMenu;
                break;
            case GREENHOUSE:
                currentMenu = greenHouse;
                break;
            case TRAVEL_LOG:
                currentMenu = travelLogMenu;
                break;
            case LEADERBOARD:
                currentMenu = leaderBoardMenu;
                break;
            case CHOOSE_PLANT:
                currentMenu = choosePlantMenu;
                break;
            case null, default:
                currentMenu = null;
        }
    }

    public void exit() {
        currentMenu = null;
    }
}