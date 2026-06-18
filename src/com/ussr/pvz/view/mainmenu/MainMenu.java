package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class MainMenu implements AppMenu {
    GlobalController globalController;
    public MainMenu() {
        this.globalController = new GlobalController();
    }
    @Override
    public void run(Scanner scanner) {
        System.out.println(globalController.handleCommand(scanner.nextLine()));
    }
}
