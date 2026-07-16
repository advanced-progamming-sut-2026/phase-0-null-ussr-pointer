package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.LevelSelectionCommand;
import com.ussr.pvz.service.LevelSelectionService;

import java.util.regex.Matcher;

public class LevelSelectionController {
    private final LevelSelectionService service = new LevelSelectionService();

    public String handleCommand(String command) {
        for (LevelSelectionCommand cmd : LevelSelectionCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case SHOW_LEVELS -> service.showLevels();
                    case SELECT_LEVEL -> service.selectLevel(matcher.group("levelId"));
                    case CONFIRM_CHEAT -> service.confirmCheat(matcher.group(1));
                };
            }
        }
        return "";
    }
}