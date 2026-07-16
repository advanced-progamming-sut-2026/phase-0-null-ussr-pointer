package com.ussr.pvz.controller.command.maincommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum LevelSelectionCommand {
    SHOW_LEVELS("^show\\s+levels(?:\\s+.*)?$"),
    SELECT_LEVEL("^select\\s+level\\s+(?<levelId>\\S+)(?:\\s+.*)?$"),
    CONFIRM_CHEAT("^(yes|no)$");

    private final String pattern;

    LevelSelectionCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}