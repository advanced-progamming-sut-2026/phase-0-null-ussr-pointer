package com.ussr.pvz.controller.command.maincommand.gamecommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ChoosePlantCommand {
    SHOW_ALL_PLANTS("^show\\s+all\\s+plants(?:\\s+.*)?$"),
    SHOW_AVAILABLE_PLANTS("^show\\s+available\\s+plants(?:\\s+.*)?$"),
    ADD_PLANT("^add\\s+plant\\s+-t\\s+\"(?<type>.+)\"(?:\\s+.*)?$"),
    REMOVE_PLANT("^remove\\s+plant\\s+-t\\s+(?<type>.+)(?:\\s+.*)?$"),
    BOOST_PLANT("^boost\\s+plant\\s+-t\\s+(?<type>.+)(?:\\s+.*)?$"),
    START_GAME("^start\\s+game(?:\\s+.*)?$");

    private final String pattern;

    ChoosePlantCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}
