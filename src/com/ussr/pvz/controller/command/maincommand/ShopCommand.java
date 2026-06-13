package com.ussr.pvz.controller.command.maincommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ShopCommand {
    SHOP_LIST("^shop\\s+list(?:\\s+.*)?$"),
    SHOP_DAILY("^shop\\s+daily(?:\\s+.*)?$"),
    SHOP_BUY("^shop\\s+buy\\s+-i\\s+(?<itemId>\\S+)\\s+-n\\s+(?<count>\\S+)(?:\\s+-t\\s+(?<plantType>\\S+))?(?:\\s+.*)?$");

    private final String pattern;

    ShopCommand(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        return Pattern.compile(this.pattern).matcher(input);
    }
}
