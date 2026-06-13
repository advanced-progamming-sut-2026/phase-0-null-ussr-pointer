package com.ussr.pvz.controller.command.maincommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TravelLogCommand {
    PAGE("^travel\\s+log\\s+page\\s+(?<pageName>\\S+)(?:\\s+.*)?$");

    private final String pattern;

    TravelLogCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}