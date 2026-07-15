package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.ShopController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class ShopMenu implements AppMenu {
    GlobalController globalController;
    ShopController shopController;

    public ShopMenu() {
        globalController = new GlobalController();
        shopController = new ShopController();
    }

    @Override
    public void run(Scanner scanner) {
        String input = scanner.nextLine();
        String output = globalController.handleCommand(input);
        if (output.isEmpty()) {
            System.out.println(shopController.handleCommand(input) + "\n");
        } else {
            System.out.println(output + "\n");
        }
    }
}