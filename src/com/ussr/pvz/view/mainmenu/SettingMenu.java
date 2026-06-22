package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.command.GlobalCommand;
import com.ussr.pvz.controller.maincontroller.SettingController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class SettingMenu implements AppMenu {
    private final GlobalController globalController = new GlobalController();
    private final SettingController settingController = new SettingController();
    @Override
    public void run(Scanner scanner) {
        String input = scanner.nextLine();
        if(globalController.handleCommand(input).isEmpty())
            System.out.println(settingController.handleCommand(input));
        else
            System.out.println(globalController.handleCommand(input));
    }
}
