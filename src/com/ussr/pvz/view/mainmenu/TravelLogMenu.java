package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.TravelLogController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class TravelLogMenu implements AppMenu {
    TravelLogController controller;
    GlobalController controllerGlobal;

    public TravelLogMenu() {
        this.controller = new TravelLogController();
        this.controllerGlobal = new GlobalController();
    }

    @Override
    public void run(Scanner scanner) {
        String input = scanner.nextLine();
        String output = controllerGlobal.handleCommand(input);
        if (output.isEmpty()) {
            System.out.println(controller.handleCommand(input));
        } else {
            System.out.println(output);
        }
    }
}
