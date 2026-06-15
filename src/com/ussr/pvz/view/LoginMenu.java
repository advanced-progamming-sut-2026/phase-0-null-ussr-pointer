package com.ussr.pvz.view;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.LoginController;

import java.util.Scanner;

public class LoginMenu implements AppMenu {
    LoginController controller;
    GlobalController controllerGlobal;

    public LoginMenu() {
        this.controller = new LoginController();
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
