package com.ussr.pvz.controller;

import com.ussr.pvz.controller.command.GlobalCommand;
import com.ussr.pvz.model.dto.*;

import java.util.regex.Matcher;

public class GlobalController {

    public GlobalController() {
    }

    public String handleCommand(String command) {
        for (GlobalCommand cmd : GlobalCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                switch (cmd) {
                    case MENU_ENTER:
                        return handleMenuEnter(matcher);
                    case MENU_SHOW_CURRENT:
                        return handleMenuShowCurrent();
                    case MENU_EXIT:
                        return handleMenuExit();
                    case MENU_LOGOUT:
                        return handleMenuLogout();
                    case ADVANCE_TIME:
                        return handleAdvanceTime(matcher);
                    case COLLECT_SUN:
                        return handleCollectSun(matcher);
                    case SHOW_SUN_AMOUNT:
                        return handleShowSunAmount();
                    case CHEAT_ADD_SUNS:
                        return handleCheatAddSuns(matcher);
                    case RELEASE_THE_NUKE:
                        return handleReleaseTheNuke();
                    case CHEAT_REMOVE_COOLDOWN:
                        return handleCheatRemoveCooldown();
                    case PLUCK_PLANT:
                        return handlePluckPlant(matcher);
                    case PLANT_PLANT:
                        return handlePlantPlant(matcher);
                    case FEED_PLANT:
                        return handleFeedPlant(matcher);
                    case CHEAT_ADD_PLANT_FOOD:
                        return handleCheatAddPlantFood();
                    case SHOW_MAP:
                        return handleShowMap();
                    case SHOW_PLANTS_STATUS:
                        return handleShowPlantsStatus();
                    case SHOW_TILE_STATUS:
                        return handleShowTileStatus(matcher);
                    case ZOMBIES_INFO:
                        return handleZombiesInfo();
                    case CHEAT_SPAWN_ZOMBIE:
                        return handleCheatSpawnZombie(matcher);
                    case CHEAT_ADD_CURRENCY:
                        return handleCheatAddCurrency(matcher);
                    case START_ZOMBIE_WAVES:
                        return handleStartZombieWaves();
                    default:
                        return "";
                }
            }
        }
        return "";
    }

    private String handleMenuEnter(Matcher matcher) {
        MenuEnterRequest request = new MenuEnterRequest(matcher.group("menuName"));
        // TODO: call globalService.menuEnter(request) and return its message
        return "";
    }

    private String handleMenuShowCurrent() {
        // TODO: call globalService.menuShowCurrent() and return its message
        return "";
    }

    private String handleMenuExit() {
        // TODO: call globalService.menuExit() and return its message
        return "";
    }

    private String handleMenuLogout() {
        // TODO: call globalService.menuLogout() and return its message
        return "";
    }

    private String handleAdvanceTime(Matcher matcher) {
        AdvanceTimeRequest request = new AdvanceTimeRequest(matcher.group("count"));
        // TODO: call globalService.advanceTime(request) and return its message
        return "";
    }

    private String handleCollectSun(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call globalService.collectSun(request) and return its message
        return "";
    }

    private String handleShowSunAmount() {
        // TODO: call globalService.showSunAmount() and return its message
        return "";
    }

    private String handleCheatAddSuns(Matcher matcher) {
        CheatAddSunsRequest request = new CheatAddSunsRequest(matcher.group("count"));
        // TODO: call globalService.cheatAddSuns(request) and return its message
        return "";
    }

    private String handleReleaseTheNuke() {
        // TODO: call globalService.releaseTheNuke() and return its message
        return "";
    }

    private String handleCheatRemoveCooldown() {
        // TODO: call globalService.cheatRemoveCooldown() and return its message
        return "";
    }

    private String handlePluckPlant(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call globalService.pluckPlant(request) and return its message
        return "";
    }

    private String handlePlantPlant(Matcher matcher) {
        PlantPlantRequest request = new PlantPlantRequest(
                matcher.group("type"),
                matcher.group("x"),
                matcher.group("y")
        );
        // TODO: call globalService.plantPlant(request) and return its message
        return "";
    }

    private String handleFeedPlant(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call globalService.feedPlant(request) and return its message
        return "";
    }

    private String handleCheatAddPlantFood() {
        // TODO: call globalService.cheatAddPlantFood() and return its message
        return "";
    }

    private String handleShowMap() {
        // TODO: call globalService.showMap() and return its message
        return "";
    }

    private String handleShowPlantsStatus() {
        // TODO: call globalService.showPlantsStatus() and return its message
        return "";
    }

    private String handleShowTileStatus(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call globalService.showTileStatus(request) and return its message
        return "";
    }

    private String handleZombiesInfo() {
        // TODO: call globalService.zombiesInfo() and return its message
        return "";
    }

    private String handleCheatSpawnZombie(Matcher matcher) {
        CheatSpawnZombieRequest request = new CheatSpawnZombieRequest(
                matcher.group("type"),
                matcher.group("x"),
                matcher.group("y")
        );
        // TODO: call globalService.cheatSpawnZombie(request) and return its message
        return "";
    }

    private String handleCheatAddCurrency(Matcher matcher) {
        CheatAddCurrencyRequest request = new CheatAddCurrencyRequest(
                matcher.group("amount"),
                matcher.group("currency")
        );
        // TODO: call globalService.cheatAddCurrency(request) and return its message
        return "";
    }

    private String handleStartZombieWaves() {
        // TODO: call globalService.startZombieWaves() and return its message
        return "";
    }
}