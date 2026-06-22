package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.GreenHouseCommand;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.dto.GreenhousePotRequest;
import com.ussr.pvz.service.GreenHouseService;
import com.ussr.pvz.controller.command.maincommand.ShopCommand;
import com.ussr.pvz.service.ShopService;

import java.util.regex.Matcher;

public class GreenHouseController {
    GreenHouseService greenHouseService;
    ShopService shopService;

    public GreenHouseController() {
        greenHouseService = new GreenHouseService();
        shopService = new ShopService();
    }

    public String handleCommand(String command) {
        for (GreenHouseCommand cmd : GreenHouseCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case SHOW_GREENHOUSE -> handleShowGreenhouse();
                    case PLANT_POT -> handlePlantPot(matcher);
                    case COLLECT -> handleCollect(matcher);
                    case GROW -> handleGrow(matcher);
                    case ENTER_SHOP -> handleEnterShop();

                };
            }
        }

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

    private String handleShowGreenhouse() {
        // TODO: call greenHouseService.showGreenhouse() and return its message
        return "";
    }

    private String handlePlantPot(Matcher matcher) {
        GreenhousePotRequest request = new GreenhousePotRequest(matcher.group("x"), matcher.group("y"));
        try {
            return greenHouseService.plant(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }


    private String handleCollect(Matcher matcher) {
        GreenhousePotRequest request = new GreenhousePotRequest(matcher.group("x"), matcher.group("y"));
        try {
            return greenHouseService.collect(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String handleGrow(Matcher matcher) {
        GreenhousePotRequest request = new GreenhousePotRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call greenHouseService.grow(request) and return its message
        return "";
    }

    private String handleEnterShop() {
        App.setMenuState(MenuState.SHOP);
        return "You are currently shopping";
    }

    private String handleShopList() {
        return shopService.shopList();
    }

    private String handleShopDaily() {
        return shopService.shopDaily();
    }

    private String handleShopBuy(Matcher matcher) {
        com.ussr.pvz.model.dto.ShopBuyRequest request = new com.ussr.pvz.model.dto.ShopBuyRequest(
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