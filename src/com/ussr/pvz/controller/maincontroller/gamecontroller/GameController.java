package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.controller.command.maincommand.gamecommand.GameCommand;
import com.ussr.pvz.model.dto.*;
import com.ussr.pvz.service.AccountService;

import java.util.regex.Matcher;

public class GameController {
    private final AccountService accountService = new AccountService();

    public GameController() {
    }

    public String handleCommand(String command) {
        for (GameCommand cmd : GameCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case MENU_ENTER_CHAPTER -> handleMenuEnterChapter(matcher);
                    case MENU_GREENHOUSE -> handleMenuGreenhouse();
                    case MENU_TRAVEL_LOG -> handleMenuTravelLog();
                    case MENU_LEADERBOARD -> handleMenuLeaderboard();
                    case MENU_COIN_WALLET -> handleMenuCoinWallet();
                    case MENU_GEM_WALLET -> handleMenuGemWallet();
                    case MENU_SWITCH_WORLD -> handleMenuSwitchWorld(matcher);
                    case MENU_LOGOUT -> handleMenuLogout();
                    case ADVANCE_TIME -> handleAdvanceTime(matcher);
                    case COLLECT_SUN -> handleCollectSun(matcher);
                    case SHOW_SUN_AMOUNT -> handleShowSunAmount();
                    case CHEAT_ADD_SUNS -> handleCheatAddSuns(matcher);
                    case RELEASE_THE_NUKE -> handleReleaseTheNuke();
                    case CHEAT_REMOVE_COOLDOWN -> handleCheatRemoveCooldown();
                    case PLUCK_PLANT -> handlePluckPlant(matcher);
                    case PLANT_PLANT -> handlePlantPlant(matcher);
                    case FEED_PLANT -> handleFeedPlant(matcher);
                    case CHEAT_ADD_PLANT_FOOD -> handleCheatAddPlantFood();
                    case SHOW_MAP -> handleShowMap();
                    case SHOW_PLANTS_STATUS -> handleShowPlantsStatus();
                    case SHOW_TILE_STATUS -> handleShowTileStatus(matcher);
                    case ZOMBIES_INFO -> handleZombiesInfo();
                    case CHEAT_SPAWN_ZOMBIE -> handleCheatSpawnZombie(matcher);
                    case CHEAT_ADD_CURRENCY -> handleCheatAddCurrency(matcher);
                    case START_ZOMBIE_WAVES -> handleStartZombieWaves();
                };
            }
        }
        return "";
    }

    private String handleMenuEnterChapter(Matcher matcher) {
        MenuEnterChapterRequest request = new MenuEnterChapterRequest(matcher.group("chapterName"));
        // TODO: call gameService.menuEnterChapter(request) and return its message
        return "";
    }

    private String handleMenuGreenhouse() {
        // TODO: call gameService.menuGreenhouse() and return its message
        return "";
    }

    private String handleMenuTravelLog() {
        // TODO: call gameService.menuTravelLog() and return its message
        return "";
    }

    private String handleMenuLeaderboard() {
        // TODO: call gameService.menuLeaderboard() and return its message
        return "";
    }

    private String handleMenuCoinWallet() {
        // TODO: call gameService.menuCoinWallet() and return its message
        return "";
    }

    private String handleMenuGemWallet() {
        // TODO: call gameService.menuGemWallet() and return its message
        return "";
    }

    private String handleMenuSwitchWorld(Matcher matcher) {
        MenuSwitchWorldRequest request = new MenuSwitchWorldRequest(matcher.group("worldName"));
        // TODO: call gameService.menuSwitchWorld(request) and return its message
        return "";
    }

    private String handleMenuLogout() {
        return accountService.logoutAccount();
    }

    private String handleAdvanceTime(Matcher matcher) {
        AdvanceTimeRequest request = new AdvanceTimeRequest(matcher.group("count"));
        // TODO: call gameService.advanceTime(request) and return its message
        return "";
    }

    private String handleCollectSun(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call gameService.collectSun(request) and return its message
        return "";
    }

    private String handleShowSunAmount() {
        // TODO: call gameService.showSunAmount() and return its message
        return "";
    }

    private String handleCheatAddSuns(Matcher matcher) {
        CheatAddSunsRequest request = new CheatAddSunsRequest(matcher.group("count"));
        // TODO: call gameService.cheatAddSuns(request) and return its message
        return "";
    }

    private String handleReleaseTheNuke() {
        // TODO: call gameService.releaseTheNuke() and return its message
        return "";
    }

    private String handleCheatRemoveCooldown() {
        // TODO: call gameService.cheatRemoveCooldown() and return its message
        return "";
    }

    private String handlePluckPlant(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call gameService.pluckPlant(request) and return its message
        return "";
    }

    private String handlePlantPlant(Matcher matcher) {
        PlantPlantRequest request = new PlantPlantRequest(
                matcher.group("type"),
                matcher.group("x"),
                matcher.group("y")
        );
        // TODO: call gameService.plantPlant(request) and return its message
        return "";
    }

    private String handleFeedPlant(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call gameService.feedPlant(request) and return its message
        return "";
    }

    private String handleCheatAddPlantFood() {
        // TODO: call gameService.cheatAddPlantFood() and return its message
        return "";
    }

    private String handleShowMap() {
        // TODO: call gameService.showMap() and return its message
        return "";
    }

    private String handleShowPlantsStatus() {
        // TODO: call gameService.showPlantsStatus() and return its message
        return "";
    }

    private String handleShowTileStatus(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        // TODO: call gameService.showTileStatus(request) and return its message
        return "";
    }

    private String handleZombiesInfo() {
        // TODO: call gameService.zombiesInfo() and return its message
        return "";
    }

    private String handleCheatSpawnZombie(Matcher matcher) {
        CheatSpawnZombieRequest request = new CheatSpawnZombieRequest(
                matcher.group("type"),
                matcher.group("x"),
                matcher.group("y")
        );
        // TODO: call gameService.cheatSpawnZombie(request) and return its message
        return "";
    }

    private String handleCheatAddCurrency(Matcher matcher) {
        CheatAddCurrencyRequest request = new CheatAddCurrencyRequest(
                matcher.group("amount"),
                matcher.group("currency")
        );
        // TODO: call gameService.cheatAddCurrency(request) and return its message
        return "";
    }

    private String handleStartZombieWaves() {
        // TODO: call gameService.startZombieWaves() and return its message
        return "";
    }
}