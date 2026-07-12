package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.ProfileController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class ProfileMenu implements AppMenu {
    private final GlobalController globalController = new GlobalController();
    private final ProfileController profileController = new ProfileController();

    @Override
    public void run(Scanner scanner) {
        String input = scanner.nextLine();
        String output = globalController.handleCommand(input);
        if (output.isEmpty()) {
            System.out.println(profileController.handleCommand(input));
        } else {
            System.out.println(output);
        }
    }
}
