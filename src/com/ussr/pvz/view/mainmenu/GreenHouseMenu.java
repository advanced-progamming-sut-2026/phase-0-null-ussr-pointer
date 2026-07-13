package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.GreenHouseController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class GreenHouseMenu implements AppMenu {
    GlobalController globalController ;
    GreenHouseController greenHouseController;
    public GreenHouseMenu() {
        globalController = new GlobalController();
        greenHouseController = new GreenHouseController();
    }
    @Override
    public void run(Scanner scanner) {
        String input = scanner.nextLine();
        String output = globalController.handleCommand(input);
        if (output.isEmpty()) {
            System.out.println(greenHouseController.handleCommand(input) + "\n");
        } else {
            System.out.println(output + "\n");
        }
    }
}
