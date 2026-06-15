package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class MainMenu implements AppMenu {
    GlobalController controller;
    public MainMenu(){
        controller = new GlobalController();
    }
    @Override
    public void run(Scanner scanner) {
        System.out.println(controller.handleCommand(scanner.nextLine()));
    }
}
