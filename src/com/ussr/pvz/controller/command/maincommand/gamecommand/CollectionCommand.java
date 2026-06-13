package com.ussr.pvz.controller.command.maincommand.gamecommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum CollectionCommand {
    SHOW_PLANTS("^menu\\s+collection\\s+show-plants(?:\\s+.*)?$"),
    SHOW_ALL_PLANTS("^menu\\s+collection\\s+show-all-plants(?:\\s+.*)?$"),
    SHOW_ZOMBIES("^menu\\s+collection\\s+show-zombies(?:\\s+.*)?$"),
    SHOW_ALL_ZOMBIES("^menu\\s+collection\\s+show-all-zombies(?:\\s+.*)?$"),
    SHOW_PLANT("^menu\\s+collection\\s+show-plant\\s+-p\\s+(?<plantName>\\S+)(?:\\s+.*)?$"),
    SHOW_ZOMBIE("^menu\\s+collection\\s+show-zombie\\s+-z\\s+(?<zombieName>\\S+)(?:\\s+.*)?$"),
    UPGRADE_PLANT("^menu\\s+collection\\s+upgrade-plant\\s+-p\\s+(?<plantName>\\S+)(?:\\s+.*)?$"),
    PURCHASE_PLANT("^menu\\s+collection\\s+purchase-plant\\s+-p\\s+(?<plantName>\\S+)(?:\\s+.*)?$");

    private final String pattern;

    CollectionCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}
