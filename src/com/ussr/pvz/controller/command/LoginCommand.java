package com.ussr.pvz.controller.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum LoginCommand {
    LOGIN("^login\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)(?<stayLoggedIn>\\s+-stay-logged-in)" +
            "?(?:\\s+.*)?$"),
    MENU_CHANGE_REGISTER("^menu\\s+change\\s+register\\s*$"),
    FORGET_PASSWORD("^forget\\s+password\\s+-u\\s+(?<username>\\S+)\\s+-e\\s+(?<email>\\S+)(?:\\s+.*)?$"),
    ANSWER("^answer\\s+-a\\s+(?<answer>\\S+)(?:\\s+.*)?$");

    private final String pattern;

    LoginCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}