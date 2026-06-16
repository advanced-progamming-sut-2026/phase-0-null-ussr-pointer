package com.ussr.pvz.controller;

import com.ussr.pvz.controller.command.GlobalCommand;
import com.ussr.pvz.model.dto.*;
import com.ussr.pvz.service.GlobalService;

import java.util.regex.Matcher;

public class GlobalController {

    private final GlobalService globalService;

    public GlobalController() {
        this.globalService = new GlobalService();
    }

    public String handleCommand(String command) {
        for (GlobalCommand cmd : GlobalCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case MENU_ENTER -> handleMenuEnter(matcher);
                    case MENU_SHOW_CURRENT -> handleMenuShowCurrent();
                    case MENU_EXIT -> handleMenuExit();
                };
            }
        }
        return "";
    }

    private String handleMenuEnter(Matcher matcher) {
        MenuEnterRequest request = new MenuEnterRequest(matcher.group("menuName"));
        return globalService.menuEnter(request);
    }

    private String handleMenuShowCurrent() {
        return globalService.menuShowCurrentMenu();
    }

    private String handleMenuExit() {
        return globalService.menuExit();
    }

}