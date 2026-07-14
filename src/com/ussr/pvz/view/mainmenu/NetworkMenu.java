package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class NetworkMenu implements AppMenu {
    @Override
    public void run(Scanner scanner) {
        System.out.println("Network features coming soon!");
        App.setMenuState(MenuState.MAIN);
    }
}