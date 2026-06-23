package com.ussr.pvz.service.game;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.dto.CheatAddCurrencyRequest;
import com.ussr.pvz.model.dto.CheatAddSunsRequest;
import com.ussr.pvz.model.dto.CheatSpawnZombieRequest;
import com.ussr.pvz.model.dto.LocationRequest;
import com.ussr.pvz.model.dto.MenuEnterChapterRequest;
import com.ussr.pvz.model.dto.MenuSwitchWorldRequest;
import com.ussr.pvz.model.dto.PlantPlantRequest;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;

public class GameService {

    public String menuEnterChapter(MenuEnterChapterRequest request) {
        // TODO: validate chapter name via LevelManager here
        App.setMenuState(MenuState.CHOOSE_PLANT);
        return "entered chapter: " + request.chapterName();
    }

    public String menuGreenhouse() {
        App.setMenuState(MenuState.GREENHOUSE);
        return "menu changed to: " + MenuState.GREENHOUSE.getName();
    }

    public String menuTravelLog() {
        App.setMenuState(MenuState.TRAVEL_LOG);
        return "menu changed to: " + MenuState.TRAVEL_LOG.getName();
    }

    public String menuLeaderboard() {
        App.setMenuState(MenuState.LEADERBOARD);
        return "menu changed to: " + MenuState.LEADERBOARD.getName();
    }

    public String menuCoinWallet() {
        Account account = App.getAccount();
        return "coin balance: " + account.getAdventureProgress().getCoin();
    }

    public String menuGemWallet() {
        Account account = App.getAccount();
        return "gem balance: " + account.getAdventureProgress().getGem();
    }

    public String menuSwitchWorld(MenuSwitchWorldRequest request) {
        // TODO: validate world name via LevelManager here
        return "switched to world: " + request.worldName();
    }

    public String collectSun(LocationRequest request) {
        int x, y;
        try {
            x = Integer.parseInt(request.x());
            y = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "invalid location";
        }
        // TODO: handle sun collection here once it's implemented
        return "sun collected at (" + x + ", " + y + ")";
    }

    public String showSunAmount() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        return "sun: " + App.getGameSession().getSunCount();
    }

    public String cheatAddSuns(CheatAddSunsRequest request) {
        int count;
        try {
            count = Integer.parseInt(request.count());
        } catch (NumberFormatException e) {
            return "invalid count";
        }
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        App.getGameSession().addSun(count * 25);
        return "added " + count + " suns";
    }

    public String releaseTheNuke() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        App.getGameSession().killAllZombies();
        return "nuke released";
    }

    public String cheatRemoveCooldown() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        App.getGameSession().removeAllCooldowns();
        return "cooldowns removed";
    }

    public String pluckPlant(LocationRequest request) {
        int x, y;
        try {
            x = Integer.parseInt(request.x());
            y = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "invalid location";
        }
        // TODO: handle the pluck here when plant service is implemented
        return "plant plucked at (" + x + ", " + y + ")";
    }

    public String plantPlant(PlantPlantRequest request) {
        int x, y;
        try {
            x = Integer.parseInt(request.x());
            y = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "invalid location";
        }
        // TODO: do other validation checks here and handle the plant process.
        return "plant " + request.type() + " placed at (" + x + ", " + y + ")";
    }

    public String feedPlant(LocationRequest request) {
        int x, y;
        try {
            x = Integer.parseInt(request.x());
            y = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "invalid location";
        }
        // TODO: handle other checks when plants are implemented.
        return "plant fed at (" + x + ", " + y + ")";
    }

    public String cheatAddPlantFood() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        App.getGameSession().addPlantFood();
        return "plant food added";
    }

    public String showMap() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        // TODO: change after zombies are implemented.
        return App.getGameSession().renderMap();
    }

    public String showPlantsStatus() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        // TODO: handle after plants are implemented.
        return App.getGameSession().renderPlantsStatus();
    }

    public String showTileStatus(LocationRequest request) {
        int x, y;
        try {
            x = Integer.parseInt(request.x());
            y = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "invalid location";
        }
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        // TODO: handle after plants and zombies are implemented.
        return App.getGameSession().renderTileStatus(x, y);
    }

    public String zombiesInfo() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        // TODO: handle after zombies are implemented.
        return App.getGameSession().renderZombiesInfo();
    }

    public String cheatSpawnZombie(CheatSpawnZombieRequest request) {
        int row;
        try {
            row = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "invalid location";
        }

        GameSession session = App.getGameSession();
        if (session == null) return "no active game session";

        Lawn lawn = session.getLawn();
        if (row < 0 || row >= lawn.getRows()) {
            return "row out of bounds (0-" + (lawn.getRows() - 1) + ")";
        }

        try {
            Zombie zombie = ZombieFactory.create(request.type(), row, lawn.getCols());
            session.spawnZombie(zombie);
            return request.type() + " spawned in row " + row;
        } catch (IllegalArgumentException e) {
            return "unknown zombie type: " + request.type();
        }
    }

    public String cheatAddCurrency(CheatAddCurrencyRequest request) {
        int amount;
        try {
            amount = Integer.parseInt(request.amount());
        } catch (NumberFormatException e) {
            return "invalid amount";
        }
        Account account = App.getAccount();
        switch (request.currency().toLowerCase()) {
            case "coin" -> {
                account.getAdventureProgress().addCoin(amount);
                return "added " + amount + " coins";
            }
            case "diamond" -> {
                account.getAdventureProgress().addGem(amount);
                return "added " + amount + " diamonds";
            }
            default -> {
                return "invalid currency type";
            }
        }
    }

    public String startZombieWaves() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        // TODO: handle after zombies and waves are implemented.
        App.getGameSession().startWaves();
        return "zombie waves started";
    }
}