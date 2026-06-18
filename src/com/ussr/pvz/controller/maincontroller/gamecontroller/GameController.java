package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.controller.command.maincommand.gamecommand.GameCommand;
import com.ussr.pvz.model.dto.CheatAddCurrencyRequest;
import com.ussr.pvz.model.dto.CheatAddSunsRequest;
import com.ussr.pvz.model.dto.CheatSpawnZombieRequest;
import com.ussr.pvz.model.dto.LocationRequest;
import com.ussr.pvz.model.dto.MenuEnterChapterRequest;
import com.ussr.pvz.model.dto.MenuSwitchWorldRequest;
import com.ussr.pvz.model.dto.PlantPlantRequest;
import com.ussr.pvz.service.AccountService;
import com.ussr.pvz.service.game.GameService;

import java.util.regex.Matcher;

public class GameController {
    private final AccountService accountService = new AccountService();
    private final GameService gameService = new GameService();

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
        return gameService.menuEnterChapter(request);
    }

    private String handleMenuGreenhouse() {
        return gameService.menuGreenhouse();
    }

    private String handleMenuTravelLog() {
        return gameService.menuTravelLog();
    }

    private String handleMenuLeaderboard() {
        return gameService.menuLeaderboard();
    }

    private String handleMenuCoinWallet() {
        return gameService.menuCoinWallet();
    }

    private String handleMenuGemWallet() {
        return gameService.menuGemWallet();
    }

    private String handleMenuSwitchWorld(Matcher matcher) {
        MenuSwitchWorldRequest request = new MenuSwitchWorldRequest(matcher.group("worldName"));
        return gameService.menuSwitchWorld(request);
    }

    private String handleMenuLogout() {
        return accountService.logoutAccount();
    }

    private String handleCollectSun(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        return gameService.collectSun(request);
    }

    private String handleShowSunAmount() {
        return gameService.showSunAmount();
    }

    private String handleCheatAddSuns(Matcher matcher) {
        CheatAddSunsRequest request = new CheatAddSunsRequest(matcher.group("count"));
        return gameService.cheatAddSuns(request);
    }

    private String handleReleaseTheNuke() {
        return gameService.releaseTheNuke();
    }

    private String handleCheatRemoveCooldown() {
        return gameService.cheatRemoveCooldown();
    }

    private String handlePluckPlant(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        return gameService.pluckPlant(request);
    }

    private String handlePlantPlant(Matcher matcher) {
        PlantPlantRequest request = new PlantPlantRequest(
                matcher.group("type"),
                matcher.group("x"),
                matcher.group("y")
        );
        return gameService.plantPlant(request);
    }

    private String handleFeedPlant(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        return gameService.feedPlant(request);
    }

    private String handleCheatAddPlantFood() {
        return gameService.cheatAddPlantFood();
    }

    private String handleShowMap() {
        return gameService.showMap();
    }

    private String handleShowPlantsStatus() {
        return gameService.showPlantsStatus();
    }

    private String handleShowTileStatus(Matcher matcher) {
        LocationRequest request = new LocationRequest(matcher.group("x"), matcher.group("y"));
        return gameService.showTileStatus(request);
    }

    private String handleZombiesInfo() {
        return gameService.zombiesInfo();
    }

    private String handleCheatSpawnZombie(Matcher matcher) {
        CheatSpawnZombieRequest request = new CheatSpawnZombieRequest(
                matcher.group("type"),
                matcher.group("x"),
                matcher.group("y")
        );
        return gameService.cheatSpawnZombie(request);
    }

    private String handleCheatAddCurrency(Matcher matcher) {
        CheatAddCurrencyRequest request = new CheatAddCurrencyRequest(
                matcher.group("amount"),
                matcher.group("currency")
        );
        return gameService.cheatAddCurrency(request);
    }

    private String handleStartZombieWaves() {
        return gameService.startZombieWaves();
    }
}