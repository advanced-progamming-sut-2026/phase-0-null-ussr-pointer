package com.ussr.pvz.controller.command.maincommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GreenHouseCommand {
    SHOW_GREENHOUSE("^show\\s+greenhouse(?:\\s+.*)?$"),
    PLANT_POT("^plant\\s+pot\\s+at\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    COLLECT("^collect\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    GROW("^grow\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    ENTER_SHOP("^enter\\s+shop(?:\\s+.*)?$");

    private final String pattern;

    GreenHouseCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}
