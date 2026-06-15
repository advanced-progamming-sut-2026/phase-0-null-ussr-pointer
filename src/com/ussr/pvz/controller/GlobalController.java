package com.ussr.pvz.controller;

import com.ussr.pvz.controller.command.GlobalCommand;
import com.ussr.pvz.model.dto.*;

import java.util.regex.Matcher;

public class GlobalController {

    public GlobalController() {
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
        // TODO: call globalService.menuEnter(request) and return its message
        return "";
    }

    private String handleMenuShowCurrent() {
        // TODO: call globalService.menuShowCurrent() and return its message
        return "";
    }

    private String handleMenuExit() {
        // TODO: call globalService.menuExit() and return its message
        return "";
    }

}