package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.controller.command.maincommand.gamecommand.GameCommand;
import com.ussr.pvz.model.dto.MenuEnterChapterRequest;
import com.ussr.pvz.model.dto.MenuSwitchWorldRequest;

import java.util.regex.Matcher;

public class GameController {

    public GameController() {
    }

    public String handleCommand(String command) {
        for (GameCommand cmd : GameCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case MENU_ENTER_CHAPTER -> handleMenuEnterChapter(matcher);
                    case MENU_GREENHOUSE -> handleMenuGreenhouse();
                    case MENU_TRAVEL_LOG -> handleMenuTravelLog();
                    case MENU_LEADERBOARD -> handleMenuLeaderboard();
                    case MENU_COIN_WALLET -> handleMenuCoinWallet();
                    case MENU_GEM_WALLET -> handleMenuGemWallet();
                    case MENU_SWITCH_WORLD -> handleMenuSwitchWorld(matcher);
                };
            }
        }
        return "";
    }

    private String handleMenuEnterChapter(Matcher matcher) {
        MenuEnterChapterRequest request = new MenuEnterChapterRequest(matcher.group("chapterName"));
        // TODO: call gameService.menuEnterChapter(request) and return its message
        return "";
    }

    private String handleMenuGreenhouse() {
        // TODO: call gameService.menuGreenhouse() and return its message
        return "";
    }

    private String handleMenuTravelLog() {
        // TODO: call gameService.menuTravelLog() and return its message
        return "";
    }

    private String handleMenuLeaderboard() {
        // TODO: call gameService.menuLeaderboard() and return its message
        return "";
    }

    private String handleMenuCoinWallet() {
        // TODO: call gameService.menuCoinWallet() and return its message
        return "";
    }

    private String handleMenuGemWallet() {
        // TODO: call gameService.menuGemWallet() and return its message
        return "";
    }

    private String handleMenuSwitchWorld(Matcher matcher) {
        MenuSwitchWorldRequest request = new MenuSwitchWorldRequest(matcher.group("worldName"));
        // TODO: call gameService.menuSwitchWorld(request) and return its message
        return "";
    }
}