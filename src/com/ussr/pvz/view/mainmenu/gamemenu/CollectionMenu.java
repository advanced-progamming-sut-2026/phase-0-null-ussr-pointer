package com.ussr.pvz.view.mainmenu.gamemenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.gamecontroller.CollectionController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class CollectionMenu implements AppMenu {
    GlobalController globalController;
    CollectionController collectionController;

    public CollectionMenu() {
        globalController = new GlobalController();
        collectionController = new CollectionController();
    }

    @Override
    public void run(Scanner scanner) {
        String input = scanner.nextLine();
        String output = globalController.handleCommand(input);

        if (output.isEmpty()) {
            System.out.println(collectionController.handleCommand(input));
        } else {
            System.out.println(output);
        }
    }
}
