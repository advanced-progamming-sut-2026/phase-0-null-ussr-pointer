package com.ussr.pvz.controller.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RegisterCommand {
    REGISTER("^register\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)\\s+(?<passwordConfirm>\\S+)" +
            "\\s+-n\\s+(?<nickname>\\S+)\\s+-e\\s+(?<email>\\S+)\\s+-g\\s+(?<gender>\\S+)(?:\\s+.*)?$"),
    PICK_QUESTION("^pick\\s+question\\s+-q\\s+(?<questionNumber>\\S+)\\s+-a\\s+(?<answer>\\S+)" +
            "\\s+-c\\s+(?<answerConfirm>\\S+)(?:\\s+.*)?$"),
    SHOW_CURRENT_MENU(GlobalCommand.MENU_SHOW_CURRENT.getPattern());

    private final String pattern;

    RegisterCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}
