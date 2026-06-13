package com.ussr.pvz.controller.command.maincommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SettingCommand {
    CHANGE_DIFFICULTY("^menu\\s+settings\\s+change-difficulty\\s+-l\\s+(?<level>\\S+)(?:\\s+.*)?$");

    private final String pattern;

    SettingCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}