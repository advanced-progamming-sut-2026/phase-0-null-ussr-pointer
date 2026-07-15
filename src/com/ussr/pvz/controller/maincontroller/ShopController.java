package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.ShopCommand;
import com.ussr.pvz.model.dto.ShopBuyRequest;
import com.ussr.pvz.service.ShopService;

import java.util.regex.Matcher;

public class ShopController {
    private final ShopService shopService = new ShopService();

    public ShopController() {
    }

    public String handleCommand(String command) {
        for (ShopCommand cmd : ShopCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case SHOP_BUY -> handleShopBuy(matcher);
                    case SHOP_LIST -> handleShopList();
                    case SHOP_DAILY -> handleShopDaily();
                };
            }
        }
        return "";
    }

    private String handleShopList() {
        return shopService.shopList();
    }

    private String handleShopDaily() {
        return shopService.shopDaily();
    }

    private String handleShopBuy(Matcher matcher) {
        ShopBuyRequest request = new ShopBuyRequest(
                matcher.group("itemId"),
                matcher.group("count"),
                matcher.group("plantType")
        );

        try {
            return shopService.buy(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}