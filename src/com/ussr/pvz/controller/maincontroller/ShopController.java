package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.ShopCommand;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.dto.ShopBuyRequest;
import com.ussr.pvz.model.shop.ShopItem;
import com.ussr.pvz.service.ShopService;

import java.util.List;
import java.util.regex.Matcher;

public class ShopController {
    private final ShopService shopService;

    public ShopController() {
        this(new ShopService());
    }

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    public String buy(ShopBuyRequest request) {
        try {
            return shopService.buy(request);
        } catch (Exception e) {
            return e.getMessage() != null ? e.getMessage() : "Purchase failed due to an unexpected error.";
        }
    }

    /**
     * Fetches all registered shop items for display.
     */
    public List<ShopItem> getShopItems() {
        shopService.ensureDailyOffersRotated();
        return App.getShopManager().getShopItems();
    }

    /**
     * Formatted catalog string representation.
     */
    public String getShopList() {
        return shopService.shopList();
    }

    /**
     * Formatted daily deals string representation.
     */
    public String getDailyShop() {
        return shopService.shopDaily();
    }

    // --- CLI Command Dispatcher ---

    public String handleCommand(String command) {
        for (ShopCommand cmd : ShopCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case SHOP_BUY -> handleShopBuy(matcher);
                    case SHOP_LIST -> getShopList();
                    case SHOP_DAILY -> getDailyShop();
                };
            }
        }
        return "";
    }

    private String handleShopBuy(Matcher matcher) {
        ShopBuyRequest request = new ShopBuyRequest(
                matcher.group("itemId"),
                matcher.group("count"),
                matcher.group("plantType")
        );
        return buy(request);
    }
}