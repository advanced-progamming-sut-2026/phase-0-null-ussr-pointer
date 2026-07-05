package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.LeaderBoardController;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

public class LeaderBoardMenu implements AppMenu {
    GlobalController globalController;
    LeaderBoardController leaderBoardController;

    public LeaderBoardMenu() {
        globalController = new GlobalController();
        leaderBoardController = new LeaderBoardController();
    }

    @Override
    public void run(Scanner scanner) {
        String input = scanner.nextLine();
        String output = globalController.handleCommand(input);
        if (output.isEmpty()) {
            System.out.println(leaderBoardController.handleCommand(input) + "\n");
        } else {
            System.out.println(output + "\n");
        }
    }
}
