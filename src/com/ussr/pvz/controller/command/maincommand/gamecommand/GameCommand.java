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
    MENU_SWITCH_WORLD("^menu\\s+switch\\s+world\\s+-w\\s+(?<worldName>\\S+)(?:\\s+.*)?$"),
    MENU_LOGOUT("^menu\\s+logout(?:\\s+.*)?$"),
    COLLECT_SUN("^collect\\s+sun\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    SHOW_SUN_AMOUNT("^show\\s+sun\\s+amount(?:\\s+.*)?$"),
    CHEAT_ADD_SUNS("^cheat\\s+add\\s+-n\\s+(?<count>\\S+)\\s+suns(?:\\s+.*)?$"),
    RELEASE_THE_NUKE("^release\\s+the\\s+nuke(?:\\s+.*)?$"),
    CHEAT_REMOVE_COOLDOWN("^cheat\\s+remove-cooldown(?:\\s+.*)?$"),
    PLUCK_PLANT("^pluck\\s+plant\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    PLANT_PLANT("^plant\\s+plant\\s+-t\\s+(?<type>\\S+)\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    FEED_PLANT("^feed\\s+plant\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    CHEAT_ADD_PLANT_FOOD("^cheat\\s+add-plant-food(?:\\s+.*)?$"),
    SHOW_MAP("^show\\s+map(?:\\s+.*)?$"),
    SHOW_PLANTS_STATUS("^show\\s+plants\\s+status(?:\\s+.*)?$"),
    SHOW_CONVEYOR("^show\\s+conveyor(?:\\s+.*)?$"),
    SHOW_TILE_STATUS("^show\\s+tile\\s+status\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    ZOMBIES_INFO("^zombies\\s+info(?:\\s+.*)?$"),
    CHEAT_SPAWN_ZOMBIE("^cheat\\s+spawn-zombie\\s+-t\\s+(?<type>\\S+)\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    CHEAT_ADD_CURRENCY("^menu\\s+cheat\\s+add\\s+(?<amount>\\S+)\\s+(?<currency>\\S+)(?:\\s+.*)?$"),

    // === Minigame Commands ===
    SWAP_PLANTS("^swap\\s+\\(\\s*(?<r1>\\S+?)\\s*,\\s*(?<c1>\\S+?)\\s*\\)\\s+with\\s+\\(\\s*(?<r2>\\S+?)\\s*,\\s*(?<c2>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    UPGRADE_BEGHOULED_PLANT("^upgrade\\s+(?<plantType>.+?)(?:\\s+.*)?$"),
    SMASH_VASE("^smash\\s+vase\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    ROLL_WALLNUT("^roll\\s+(?<type>\\S+)\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    PLACE_ZOMBIE("^place\\s+zombie\\s+-t\\s+(?<type>\\S+)\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),

    START_ZOMBIE_WAVES("^start\\s+zombie\\s+waves(?:\\s+.*)?$");

    private final String pattern;

    GameCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}