package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.GreenHouseCommand;
import com.ussr.pvz.model.dto.GreenhousePotRequest;
import com.ussr.pvz.service.GreenHouseService;

import java.util.regex.Matcher;

public class GreenHouseController {
    GreenHouseService greenHouseService;

    public GreenHouseController() {
        greenHouseService = new GreenHouseService();
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
        return "";
    }

    private String handleShowGreenhouse() {
        // TODO: call greenHouseService.showGreenhouse() and return its message
        return "";
    }

    private String handlePlantPot(Matcher matcher) {
        GreenhousePotRequest request = new GreenhousePotRequest(matcher.group("x"), matcher.group("y"));
        return greenHouseService.plant(request);
    }

    private String handleCollect(Matcher matcher) {
        GreenhousePotRequest request = new GreenhousePotRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call greenHouseService.collect(request) and return its message
        return "";
    }

    private String handleGrow(Matcher matcher) {
        GreenhousePotRequest request = new GreenhousePotRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call greenHouseService.grow(request) and return its message
        return "";
    }

    private String handleEnterShop() {
        // TODO: call greenHouseService.enterShop() and return its message
        return "";
    }

    // Add inside GreenHouseController if shop stays here:

    private String handleShopList() {
        // TODO: call greenHouseService.shopList() and return its message
        return "";
    }

    private String handleShopDaily() {
        // TODO: call greenHouseService.shopDaily() and return its message
        return "";
    }

    private String handleShopBuy(Matcher matcher) {
        com.ussr.pvz.model.dto.ShopBuyRequest request = new com.ussr.pvz.model.dto.ShopBuyRequest(
                matcher.group("itemId"),
                matcher.group("count"),
                matcher.group("plantType")
        );
        // TODO: call greenHouseService.shopBuy(request) and return its message
        return "";
    }
}