package com.ussr.pvz.view.mainmenu.gamemenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.gamecontroller.ChoosePlantController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class ChoosePlantMenu implements AppMenu {
    private final ChoosePlantController controller = new ChoosePlantController();
    private final GlobalController globalController = new GlobalController();

    @Override
    public void run(Scanner scanner) {
        String input = scanner.nextLine();
        String output = globalController.handleCommand(input);
        if (output.isEmpty()) {
            System.out.println(controller.handleCommand(input));
        } else {
            System.out.println(output);
        }
    }
}