package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.NewsController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class NewsMenu implements AppMenu {
    NewsController controller;
    GlobalController controllerGlobal;

    public NewsMenu() {
        this.controller = new NewsController();
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
