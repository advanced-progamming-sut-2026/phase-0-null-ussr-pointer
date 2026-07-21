package com.ussr.pvz.controller.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GlobalCommand {
    MENU_ENTER("^menu\\s+enter\\s+(?!chapter|greenhouse|travel-log|leaderboard|coin-wallet|gem-wallet|switch|logout)(?<menuName>\\S+)(?:\\s+.*)?$"),
    MENU_SHOW_CURRENT("^menu\\s+show\\s+current(?:\\s+.*)?$"),
    ADVANCE_TIME("^advance\\s+time\\s+-t\\s+(?<count>\\S+)\\s+ticks(?:\\s+.*)?$"),
    MENU_LOGOUT("^menu\\s+logout(?:\\s+.*)?$"),
    MENU_QUIT("^menu\\s+quit(?:\\s+.*)?$"),
    MENU_SHOW_ALL("^menu\\s+show\\s+all(?:\\s+.*)?$"),
    MENU_EXIT("^menu\\s+exit(?:\\s+.*)?$");

    private final String pattern;

    GlobalCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }

    public String getPattern() {
        return this.pattern;
    }
}
