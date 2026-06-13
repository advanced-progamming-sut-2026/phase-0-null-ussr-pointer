package com.ussr.pvz.controller.command.maincommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum NewsCommand {
    SHOW_UNREAD("^menu\\s+news\\s+show-unread(?:\\s+.*)?$"),
    SHOW_ALL("^menu\\s+news\\s+show-all(?:\\s+.*)?$");

    private final String pattern;

    NewsCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}
