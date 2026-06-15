package com.ussr.pvz.view;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.RegisterController;

import java.util.Scanner;

public class RegisterMenu implements AppMenu {
    RegisterController controller;
    GlobalController controllerGlobal;

    public RegisterMenu() {
        controller = new RegisterController();
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
