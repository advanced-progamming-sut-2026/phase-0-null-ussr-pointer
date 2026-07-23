package com.ussr.pvz.controller.command.maincommand.gamecommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GameCommand {
    MENU_ENTER_CHAPTER("^menu\\s+enter\\s+chapter\\s+-c\\s+(?<chapterName>\\S+)(?:\\s+.*)?$"),
    MENU_ENTER_MEOW("^menu\\s+enter\\s+meow(?:\\s+.*)?$"),
    MENU_GREENHOUSE("^menu\\s+greenhouse(?:\\s+.*)?$"),
    MENU_TRAVEL_LOG("^menu\\s+travel-log(?:\\s+.*)?$"),
    MENU_LEADERBOARD("^menu\\s+leaderboard(?:\\s+.*)?$"),
    MENU_COIN_WALLET("^menu\\s+coin-wallet(?:\\s+.*)?$"),
    MENU_GEM_WALLET("^menu\\s+gem-wallet(?:\\s+.*)?$"),
    MENU_SWITCH_WORLD("^menu\\s+switch\\s+world\\s+-w\\s+(?<worldName>\\S+)(?:\\s+.*)?$"),
    COLLECT_SUN("^collect\\s+sun\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)(?:\\s+.*)?$"),
    SHOW_SUN_AMOUNT("^show\\s+sun\\s+amount(?:\\s+.*)?$"),
    CHEAT_ADD_SUNS("^cheat\\s+add\\s+-n\\s+(?<count>\\S+)\\s+suns(?:\\s+.*)?$"),
    RELEASE_THE_NUKE("^release\\s+the\\s+nuke(?:\\s+.*)?$"),
    CHEAT_REMOVE_COOLDOWN("^cheat\\s+remove-cooldown(?:\\s+.*)?$"),
    PLUCK_PLANT("^pluck\\s+plant\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)(?:\\s+.*)?$"),
    PLANT_PLANT("^plant\\s+plant\\s+-t\\s+(?<type>.+?)\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*," +
            "\\s*(?<y>\\d+)\\s*\\)(?:\\s+.*)?$"),
    FEED_PLANT("^feed\\s+plant\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)(?:\\s+.*)?$"),
    CHEAT_ADD_PLANT_FOOD("^cheat\\s+add-plant-food(?:\\s+.*)?$"),
    SHOW_MAP("^show\\s+map(?:\\s+.*)?$"),
    SHOW_PLANTS_STATUS("^show\\s+plants\\s+status(?:\\s+.*)?$"),
    SHOW_CONVEYOR("^show\\s+conveyor(?:\\s+.*)?$"),
    SHOW_TILE_STATUS("^show\\s+tile\\s+status\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)(?:\\s+.*)?$"),
    ZOMBIES_INFO("^zombies\\s+info(?:\\s+.*)?$"),
    SHOW_PLANT_FOOD("^show\\s+plant\\s+foods?(?:\\s+.*)?$"),
    CHEAT_SPAWN_ZOMBIE("^cheat\\s+spawn-zombie\\s+-t\\s+(?<type>.+?)\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*," +
            "\\s*(?<y>\\d+)\\s*\\)(?:\\s+(?<glowing>-g))?(?:\\s+.*)?$"),
    CHEAT_DECREASE_HEALTH("^cheat\\s+decrease-health\\s+-t\\s+(?<type>.+?)\\s+-a\\s+(?<amount>\\d+)" +
            "(?:\\s+.*)?$"),
    CHEAT_UNLOCK_ALL("^cheat\\s+unlock-all(?:\\s+.*)?$"),
    CHEAT_ADD_CURRENCY("^menu\\s+cheat\\s+add\\s+(?<amount>\\d+)\\s+(?<currency>\\S+)(?:\\s+.*)?$"),
    CHEAT_COMPLETE_QUEST("^cheat\\s+complete-quest\\s+-q\\s+(?<questId>\\S+)(?:\\s+.*)?$"),

    // === Minigame Commands ===
    SWAP_PLANTS("^swap\\s+\\(\\s*(?<r1>\\d+)\\s*,\\s*(?<c1>\\d+)\\s*\\)\\s+with\\s+\\(\\s*(?<r2>\\d+)\\s*," +
            "\\s*(?<c2>\\d+)\\s*\\)(?:\\s+.*)?$"),
    UPGRADE_BEGHOULED_PLANT("^upgrade\\s+(?<plantType>.+?)(?:\\s+.*)?$"),
    SMASH_VASE("^smash\\s+vase\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)(?:\\s+.*)?$"),
    ROLL_WALLNUT("^roll\\s+(?<type>\\S+)\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)(?:\\s+.*)?$"),
    PLACE_ZOMBIE("^place\\s+zombie\\s+-t\\s+(?<type>.+?)\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)" +
            "(?:\\s+.*)?$"),
    PLANT_FROM_SEEDPACK("^plant\\s+from\\s*\\(\\s*(?<sx>\\d+)\\s*,\\s*(?<sy>\\d+)\\s*\\)\\s+to\\s*" +
            "\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)(?:\\s+.*)?$"),
    START_ZOMBIE_WAVES("^start\\s+zombie\\s+waves(?:\\s+.*)?$");

    private final String pattern;

    GameCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}