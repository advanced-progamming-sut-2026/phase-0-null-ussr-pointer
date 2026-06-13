package com.ussr.pvz.controller.command.maincommand.gamecommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GameCommand {
    MENU_ENTER_CHAPTER("^menu\\s+enter\\s+chapter\\s+-c\\s+(?<chapterName>\\S+)(?:\\s+.*)?$"),
    MENU_GREENHOUSE("^menu\\s+greenhouse(?:\\s+.*)?$"),
    MENU_TRAVEL_LOG("^menu\\s+travel-log(?:\\s+.*)?$"),
    MENU_LEADERBOARD("^menu\\s+leaderboard(?:\\s+.*)?$"),
    MENU_COIN_WALLET("^menu\\s+coin-wallet(?:\\s+.*)?$"),
    MENU_GEM_WALLET("^menu\\s+gem-wallet(?:\\s+.*)?$"),
    MENU_SWITCH_WORLD("^menu\\s+switch\\s+world\\s+-w\\s+(?<worldName>\\S+)(?:\\s+.*)?$");

    private final String pattern;

    GameCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}
