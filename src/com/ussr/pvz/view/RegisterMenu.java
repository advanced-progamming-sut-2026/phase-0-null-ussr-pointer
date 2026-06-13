package com.ussr.pvz.view;

import com.ussr.pvz.controller.RegisterController;

import java.util.Scanner;

public class RegisterMenu implements AppMenu {
    RegisterController controller;

    public RegisterMenu() {
        controller = new RegisterController();
    }

    @Override
    public void run(Scanner scanner) {
        String input = scanner.nextLine();
        System.out.println(controller.handleCommand(input));
    }
}
