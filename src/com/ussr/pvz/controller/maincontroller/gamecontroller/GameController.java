package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.controller.command.maincommand.gamecommand.GameCommand;
import com.ussr.pvz.model.dto.CheatAddCurrencyRequest;
import com.ussr.pvz.model.dto.CheatAddSunsRequest;
import com.ussr.pvz.model.dto.CheatSpawnZombieRequest;
import com.ussr.pvz.model.dto.LocationRequest;
import com.ussr.pvz.model.dto.MenuEnterChapterRequest;
import com.ussr.pvz.model.dto.MenuSwitchWorldRequest;
import com.ussr.pvz.model.dto.PlantPlantRequest;
import com.ussr.pvz.service.game.GameService;
import com.ussr.pvz.service.minigame.BeghouledService;
import com.ussr.pvz.service.minigame.IZombieService;
import com.ussr.pvz.service.minigame.VaseBreakerService;
import com.ussr.pvz.service.minigame.WallnutBowlingService;
import java.util.regex.Matcher;

public class GameController {
    private final GameService gameService = new GameService();

    private final BeghouledService beghouledService = new BeghouledService();
    private final VaseBreakerService vaseBreakerService = new VaseBreakerService();
    private final WallnutBowlingService bowlingService = new WallnutBowlingService();
    private final IZombieService iZombieService = new IZombieService();

    public GameController() {
    }

    public String handleCommand(String command) {
        for (GameCommand cmd : GameCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                String result = routeMenuAndInformationCommands(cmd, matcher);
                if (!result.isEmpty()) return result;
                return routeActionAndCheatCommands(cmd, matcher);
            }
        }
        return "";
    }

    private String routeMenuAndInformationCommands(GameCommand cmd, Matcher matcher) {
        return switch (cmd) {
            case MENU_ENTER_CHAPTER -> handleMenuEnterChapter(matcher);
            case MENU_ENTER_MEOW -> handleMenuEnterMeow();
            case MENU_GREENHOUSE -> handleMenuGreenhouse();
            case MENU_TRAVEL_LOG -> handleMenuTravelLog();
            case MENU_LEADERBOARD -> handleMenuLeaderboard();
            case MENU_COIN_WALLET -> handleMenuCoinWallet();
            case MENU_GEM_WALLET -> handleMenuGemWallet();
            case MENU_SWITCH_WORLD -> handleMenuSwitchWorld(matcher);
            case SHOW_SUN_AMOUNT -> handleShowSunAmount();
            case SHOW_MAP -> handleShowMap();
            case SHOW_PLANTS_STATUS -> handleShowPlantsStatus();
            case SHOW_TILE_STATUS -> handleShowTileStatus(matcher);
            case ZOMBIES_INFO -> handleZombiesInfo();
            case SHOW_CONVEYOR -> handleShowConveyor();
            case SHOW_PLANT_FOOD -> handleShowPlantFood();
            default -> "";
        };
    }

    private String routeActionAndCheatCommands(GameCommand cmd, Matcher matcher) {
        return switch (cmd) {
            case COLLECT_SUN -> handleCollectSun(matcher);
            case CHEAT_ADD_SUNS -> handleCheatAddSuns(matcher);
            case RELEASE_THE_NUKE -> handleReleaseTheNuke();
            case CHEAT_REMOVE_COOLDOWN -> handleCheatRemoveCooldown();
            case PLUCK_PLANT -> handlePluckPlant(matcher);
            case PLANT_PLANT -> handlePlantPlant(matcher);
            case FEED_PLANT -> handleFeedPlant(matcher);
            case CHEAT_ADD_PLANT_FOOD -> handleCheatAddPlantFood();
            case CHEAT_SPAWN_ZOMBIE -> handleCheatSpawnZombie(matcher);
            case CHEAT_DECREASE_HEALTH -> handleCheatDecreaseHealth(matcher);
            case CHEAT_UNLOCK_ALL -> handleCheatUnlockAll();
            case CHEAT_ADD_CURRENCY -> handleCheatAddCurrency(matcher);
            case CHEAT_COMPLETE_QUEST -> handleCheatCompleteQuest(matcher);
            case START_ZOMBIE_WAVES -> handleStartZombieWaves();

            // Minigame Integrations
            case SWAP_PLANTS -> handleSwapPlants(matcher);
            case UPGRADE_BEGHOULED_PLANT -> handleUpgradeBeghouledPlant(matcher);
            case SMASH_VASE -> handleSmashVase(matcher);
            case ROLL_WALLNUT -> handleRollWallnut(matcher);
            case PLACE_ZOMBIE -> handlePlaceZombie(matcher);
            case PLANT_FROM_SEEDPACK -> handlePlantFromSeedpack(matcher);

            default -> "";
        };
    }

    private String handleMenuEnterMeow() {
        return gameService.menuEnterMeow();
    }

    private String handleSwapPlants(Matcher matcher) {
        try {
            int r1 = Integer.parseInt(matcher.group("r1"));
            int c1 = Integer.parseInt(matcher.group("c1"));
            int r2 = Integer.parseInt(matcher.group("r2"));
            int c2 = Integer.parseInt(matcher.group("c2"));
            return beghouledService.swapPlants(r1, c1, r2, c2);
        } catch (NumberFormatException e) {
            return "invalid location coordinates";
        }
    }

    private String handleUpgradeBeghouledPlant(Matcher matcher) {
        String plantType = matcher.group("plantType");
        return beghouledService.upgradePlant(plantType);
    }

    private String handleSmashVase(Matcher matcher) {
        try {
            int x = Integer.parseInt(matcher.group("x"));
            int y = Integer.parseInt(matcher.group("y"));
            return vaseBreakerService.smashVase(x, y);
        } catch (NumberFormatException e) {
            return "invalid location coordinates";
        }
    }

    private String handleRollWallnut(Matcher matcher) {
        try {
            String type = matcher.group("type");
            int x = Integer.parseInt(matcher.group("x"));
            int y = Integer.parseInt(matcher.group("y"));
            return bowlingService.rollWallnut(type, x, y);
        } catch (NumberFormatException e) {
            return "invalid location coordinates";
        }
    }

    private String handlePlaceZombie(Matcher matcher) {
        try {
            String type = matcher.group("type");
            int x = Integer.parseInt(matcher.group("x"));
            int y = Integer.parseInt(matcher.group("y"));
            return iZombieService.placeZombie(type, x, y);
        } catch (NumberFormatException e) {
            return "invalid location coordinates";
        }
    }

    // === STANDARD HANDLERS ===

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
        boolean glowing = matcher.group("glowing") != null;
        CheatSpawnZombieRequest request = new CheatSpawnZombieRequest(
                matcher.group("type"),
                matcher.group("x"),
                matcher.group("y"),
                glowing
        );
        return gameService.cheatSpawnZombie(request);
    }

    private String handleCheatDecreaseHealth(Matcher matcher) {
        try {
            String type = matcher.group("type");
            int amount = Integer.parseInt(matcher.group("amount"));
            return gameService.cheatDecreaseHealth(type, amount);
        } catch (NumberFormatException e) {
            return "invalid health amount";
        }
    }

    private String handleCheatUnlockAll() {
        return gameService.cheatUnlockAll();
    }

    private String handleCheatAddCurrency(Matcher matcher) {
        CheatAddCurrencyRequest request = new CheatAddCurrencyRequest(
                matcher.group("amount"),
                matcher.group("currency")
        );
        return gameService.cheatAddCurrency(request);
    }

    private String handleCheatCompleteQuest(Matcher matcher) {
        String questId = matcher.group("questId");
        return gameService.cheatCompleteQuest(questId);
    }

    private String handleStartZombieWaves() {
        return gameService.startZombieWaves();
    }

    private String handlePlantFromSeedpack(Matcher matcher) {
        try {
            int sx = Integer.parseInt(matcher.group("sx"));
            int sy = Integer.parseInt(matcher.group("sy"));
            int x = Integer.parseInt(matcher.group("x"));
            int y = Integer.parseInt(matcher.group("y"));
            return vaseBreakerService.plantFromSeedPack(sx, sy, x, y);
        } catch (NumberFormatException e) {
            return "invalid location coordinates";
        }
    }

    private String handleShowConveyor() {
        return gameService.showConveyor();
    }

    private String handleShowPlantFood() {
        return gameService.showPlantFood();
    }
}