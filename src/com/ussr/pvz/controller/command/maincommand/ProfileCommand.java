package com.ussr.pvz.controller.command.maincommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ProfileCommand {
    CHANGE_USERNAME("^menu\\s+profile\\s+change-username\\s+-u\\s+(?<username>\\S+)(?:\\s+.*)?$"),
    CHANGE_NICKNAME("^menu\\s+profile\\s+change-nickname\\s+-u\\s+(?<nickname>\\S+)(?:\\s+.*)?$"),
    CHANGE_EMAIL("^menu\\s+profile\\s+change-email\\s+-e\\s+(?<email>\\S+)(?:\\s+.*)?$"),
    CHANGE_PASSWORD("^menu\\s+profile\\s+change-password\\s+-p\\s+(?<newPassword>\\S+)\\s+-o\\s+" +
            "(?<oldPassword>\\S+)(?:\\s+.*)?$"),
    SHOW_INFO("^menu\\s+profile\\s+show-info(?:\\s+.*)?$");

    private final String pattern;

    ProfileCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}