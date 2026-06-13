package com.ussr.pvz.controller.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GlobalCommand {
    MENU_ENTER("^menu\\s+enter\\s+(?<menuName>\\S+)(?:\\s+.*)?$"),
    MENU_SHOW_CURRENT("^menu\\s+show\\s+current(?:\\s+.*)?$"),
    MENU_EXIT("^menu\\s+exit(?:\\s+.*)?$"),
    MENU_LOGOUT("^menu\\s+logout(?:\\s+.*)?$"),
    ADVANCE_TIME("^advance\\s+time\\s+-t\\s+(?<count>\\S+)\\s+ticks(?:\\s+.*)?$"),
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
    SHOW_TILE_STATUS("^show\\s+tile\\s+status\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    ZOMBIES_INFO("^zombies\\s+info(?:\\s+.*)?$"),
    CHEAT_SPAWN_ZOMBIE("^cheat\\s+spawn-zombie\\s+-t\\s+(?<type>\\S+)\\s+-l\\s*\\(\\s*(?<x>\\S+?)\\s*,\\s*(?<y>\\S+?)\\s*\\)(?:\\s+.*)?$"),
    CHEAT_ADD_CURRENCY("^menu\\s+cheat\\s+add\\s+(?<amount>\\S+)\\s+(?<currency>\\S+)(?:\\s+.*)?$"),
    START_ZOMBIE_WAVES("^start\\s+zombie\\s+waves(?:\\s+.*)?$");

    private final String pattern;

    GlobalCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}
