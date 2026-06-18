package com.ussr.pvz.view.mainmenu.gamemenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class GameMenu implements AppMenu {
    private final GameController controller;
    private final GlobalController controllerGlobal;

    public GameMenu() {
        this.controller = new GameController();
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