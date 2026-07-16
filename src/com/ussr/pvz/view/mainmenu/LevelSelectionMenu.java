package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.LevelSelectionController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class LevelSelectionMenu implements AppMenu {
    private final GlobalController globalController = new GlobalController();
    private final LevelSelectionController selectionController = new LevelSelectionController();

    @Override
    public void run(Scanner scanner) {
        String input = scanner.nextLine();

        // Pass to global controller first so "menu exit" functions perfectly
        String output = globalController.handleCommand(input);

        if (output.isEmpty()) {
            System.out.println(selectionController.handleCommand(input));
        } else {
            System.out.println(output);
        }
    }
}