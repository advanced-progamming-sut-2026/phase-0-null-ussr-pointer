package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.controller.command.maincommand.GreenHouseCommand;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
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
        return greenHouseService.showGreenHouse();
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
        try {
            return greenHouseService.grow(request);
        }catch (Exception e) {
            return e.getMessage();
        }
    }

    private String handleEnterShop() {
        App.setMenuState(MenuState.SHOP);
        return "You are currently shopping";
    }
}