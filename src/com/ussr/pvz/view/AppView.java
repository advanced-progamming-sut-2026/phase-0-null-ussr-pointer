package com.ussr.pvz.view;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.view.mainmenu.*;
import com.ussr.pvz.view.mainmenu.gamemenu.ChoosePlantMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.CollectionMenu;
import com.ussr.pvz.view.mainmenu.gamemenu.GameMenu;

import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;

public class AppView {
    private AppMenu currentMenu;
    private final Map<MenuState, AppMenu> menus = new EnumMap<>(MenuState.class);

    public AppView() {
        menus.put(MenuState.MAIN, new MainMenu());
        menus.put(MenuState.REGISTER, new RegisterMenu());
        menus.put(MenuState.LOGIN, new LoginMenu());
        menus.put(MenuState.GAME, new GameMenu());
        menus.put(MenuState.NEWS, new NewsMenu());
        menus.put(MenuState.NETWORK, new NetworkMenu());
        menus.put(MenuState.PROFILE, new ProfileMenu());
        menus.put(MenuState.SETTING, new SettingMenu());
        menus.put(MenuState.COLLECTION, new CollectionMenu());
        menus.put(MenuState.GREENHOUSE, new GreenHouseMenu());
        menus.put(MenuState.TRAVEL_LOG, new TravelLogMenu());
        menus.put(MenuState.LEADERBOARD, new LeaderBoardMenu());
        menus.put(MenuState.CHOOSE_PLANT, new ChoosePlantMenu());

        App.initShop();
        App.getLevelManager().loadFromJson();
    }

    public void run(Scanner scanner) {
        while (true) {
            setCurrentMenu(App.getMenuState());
            if (currentMenu != null) {
                currentMenu.run(scanner);
            } else {
                break;
            }
        }
    }

    public void setCurrentMenu(MenuState menuState) {
        currentMenu = menuState != null ? menus.get(menuState) : null;
    }

    public void exit() {
        currentMenu = null;
    }
}