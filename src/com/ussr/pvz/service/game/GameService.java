package com.ussr.pvz.service.game;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.dto.CheatAddCurrencyRequest;
import com.ussr.pvz.model.dto.CheatAddSunsRequest;
import com.ussr.pvz.model.dto.CheatSpawnZombieRequest;
import com.ussr.pvz.model.dto.LocationRequest;
import com.ussr.pvz.model.dto.MenuEnterChapterRequest;
import com.ussr.pvz.model.dto.MenuSwitchWorldRequest;
import com.ussr.pvz.model.dto.PlantPlantRequest;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodType;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.Level;

public class GameService {

    public String menuEnterChapter(MenuEnterChapterRequest request) {
        String chapterId = request.chapterName();

        // 1. Block Minigames from Adventure access
        if (chapterId.toLowerCase().contains("minigame")) {
            return "Minigames can only be accessed from the Travel Log.";
        }

        if (App.getLevelManager().findChapter(chapterId) == null) {
            return "chapter not found: " + chapterId;
        }

        try {
            App.getLevelManager().startChapter(chapterId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "could not enter chapter '" + chapterId + "': " + e.getMessage();
        }

        // 2. Go to Level Selection instead of Choose Plant directly
        App.setMenuState(MenuState.LEVEL_SELECTION);
        return "entered chapter: " + request.chapterName() + ". Type 'show levels' to see options.";
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
        String worldId = request.worldName();

        // "worlds" map onto chapters in this codebase (there is no separate World model),
        // so we validate/switch the same way menuEnterChapter does, via LevelManager.
        if (App.getLevelManager().findChapter(worldId) == null) {
            return "world not found: " + worldId;
        }

        try {
            App.getLevelManager().startChapter(worldId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "could not switch to world '" + worldId + "': " + e.getMessage();
        }

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

        var matchingSun = App.getGameSession().getItems().stream()
                .filter(item -> item.getItemType() == ItemType.SUN)
                .filter(item -> !item.isCollected())
                .filter(item -> item.getLocation().equals(new GroundItem.Location(x, y)))
                .findFirst();

        if (matchingSun.isPresent()) {
            matchingSun.get().collect();
            return "sun collected at (" + x + ", " + y + ")";
        }

        return "no sun found at (" + x + ", " + y + ")";
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

        boolean success = App.getGameSession().removePlantAt(x, y);

        if (success) {
            return String.format("plant plucked at (%d, %d)", x, y);
        }

        return "no plant found at that location";
    }

    public String plantPlant(PlantPlantRequest request) {
        int x, y;
        try {
            x = Integer.parseInt(request.x());
            y = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "invalid location";
        }

        try {
            GameSession session = requireSession();
            Plant blueprint = requireUnlockedPlant(request.type());

            Cell cell = requirePlantableCell(session, x, y, blueprint);

            if (blueprint.getRecharge() > 0) {
                throw new IllegalStateException("Plant is refreshing! Please wait.");
            }

            if (!session.spendSun(blueprint.getCost())) {
                throw new IllegalStateException(
                        "not enough sun to plant " + blueprint.getName() + " (needs " + blueprint.getCost() + ")");
            }

            Plant plant = instantiatePlant(blueprint, x, y);

            if (App.getAccount().getSavedBoosts() != null && App.getAccount().getSavedBoosts().useBoost(blueprint.getName())) {
                if (plant.getPlantFoodType() != null && plant.getPlantFoodType() != PlantFoodType.NONE && plant.getPlantFoodEffect() != null) {
                    plant.getPlantFoodEffect().triggerSuperpower(plant, session);
                    plant.getPlantFoodEffect().applyStatusModifiers(plant);
                    session.notifyPlantFoodUsed(plant);
                }
            }

            if (!cell.isEmpty()) {
                Plant existingLilyPad = cell.getPlant();
                plant.setBottom(existingLilyPad);
            }

            cell.setPlant(plant);
            session.addPlant(plant);

            // TODO: [MINT FAMILY BUFFS]
            // 1. Check if 'plant' is a Mint (e.g., blueprint.getAbilityType() == MINT_FAMILY_BOOST).
            // 2. If true, iterate over session.getPlants(), match family tags, and apply a StatModifier
            //    to their ModifiableStat fields (damage, attack speed, etc.).
            // 3. Mark the Mint plant to despawn/setAlive(false) after its active duration expires.

            blueprint.setRecharge(blueprint.getMaxRecharge());

            return "plant " + plant.getName() + " placed at (" + x + ", " + y + ")";
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    private GameSession requireSession() {
        GameSession session = App.getGameSession();
        if (session == null) {
            throw new IllegalStateException("no active game session");
        }
        return session;
    }

    private Cell requirePlantableCell(GameSession session, int x, int y, Plant blueprint) {
        Lawn lawn = session.getLawn();
        if (lawn == null || y < 0 || y >= lawn.getRows() || x < 0 || x >= lawn.getCols()) {
            throw new IllegalStateException("invalid location");
        }

        Cell cell = lawn.getCell(y, x);
        if (cell == null) {
            throw new IllegalStateException("invalid location");
        }

        boolean isWaterTile = cell.getTile() != null && cell.getTile().getType() == com.ussr.pvz.model.board.terrain.TileType.Water;
        boolean isAquaticPlant = blueprint.getTags().contains(com.ussr.pvz.model.entities.plants.Tag.WATER) ||
                blueprint.getName().equalsIgnoreCase("Lily Pad") ||
                blueprint.getName().equalsIgnoreCase("LilyPad");

        if (cell.getTile() != null && !cell.getTile().allowsPlant()) {
            if (!(isWaterTile && isAquaticPlant)) {
                throw new IllegalStateException("cannot plant " + blueprint.getName() + " on this tile (" + x + ", " + y + ")");
            }
        } else if (isWaterTile && !isAquaticPlant && cell.isEmpty()) {
            throw new IllegalStateException(blueprint.getName() + " must be planted on a Lily Pad!");
        }

        if (!cell.isEmpty()) {
            Plant existingPlant = cell.getPlant();
            boolean isLilyPad = existingPlant.getName().equalsIgnoreCase("Lily Pad") || existingPlant.getName().equalsIgnoreCase("LilyPad");

            if (isLilyPad && !isAquaticPlant) {
                // This is allowed, do nothing here.
            } else {
                throw new IllegalStateException("a plant is already at (" + x + ", " + y + ")");
            }
        }
        return cell;
    }

    private Plant requireUnlockedPlant(String requestedType) {
        Account account = App.getAccount();
        if (account == null) {
            throw new IllegalStateException("no active account");
        }

        String plantKey = requestedType == null ? "" : requestedType.trim().toUpperCase().replaceAll("[\\s_]", "");
        if (account.getAdventureProgress().getPlantLvls().get(plantKey) == 0) {
            throw new IllegalStateException("You haven't unlocked " + requestedType);
        }
        return PlantFactory.createPlant(PlantFactory.findIdByName(requestedType), account.getAdventureProgress().getPlantLvls().get(plantKey));
    }

    private Plant instantiatePlant(Plant blueprint, int x, int y) {
        Plant plant = new Plant(blueprint);
        plant.setLocation(new Plant.Location(x, y));
        plant.setState(Plant.PlantState.ACTIVE);
        plant.setAlive(true);
        return plant;
    }

    public String feedPlant(LocationRequest request) {
        int x, y;
        try {
            x = Integer.parseInt(request.x());
            y = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "invalid location";
        }

        try {
            GameSession session = requireSession();
            Plant plant = requireFeedablePlant(session, x, y);

            if (!session.spendPlantFood()) {
                throw new IllegalStateException("no plant food available");
            }

            plant.getPlantFoodEffect().triggerSuperpower(plant, session);
            plant.getPlantFoodEffect().applyStatusModifiers(plant);
            session.notifyPlantFoodUsed(plant);

            return "plant fed at (" + x + ", " + y + ")";
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    private Plant requireFeedablePlant(GameSession session, int x, int y) {
        Lawn lawn = session.getLawn();
        if (lawn == null) {
            throw new IllegalStateException("map not initialized");
        }

        Cell cell = lawn.getCell(y, x);
        if (cell == null || cell.getPlant() == null) {
            throw new IllegalStateException("no plant found at that location");
        }

        Plant plant = cell.getPlant();
        if (plant.getPlantFoodType() == null
                || plant.getPlantFoodType() == PlantFoodType.NONE
                || plant.getPlantFoodEffect() == null) {
            throw new IllegalStateException(plant.getName() + " has no plant food effect");
        }
        if (plant.getPlantFoodTimer() > 0) {
            throw new IllegalStateException(plant.getName() + " is already under a plant food effect");
        }
        return plant;
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
        return App.getGameSession().renderMap();
    }

    public String showPlantsStatus() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        return App.getGameSession().renderPlantsStatus();
    }

    public String showConveyor() {
        GameSession session = App.getGameSession();
        if (session == null || session.getLevel() == null) {
            return "no active game session";
        }

        if (session.getLevel().getDeliveryStrategy() instanceof com.ussr.pvz.model.level.delivery.ConveyorDeliveryStrategy conveyor) {
            if (conveyor.getConveyorBelt().isEmpty()) {
                return "conveyor is empty";
            }
            return "Conveyor Belt: [ " + String.join(", ", conveyor.getConveyorBelt()) + " ]";
        }

        return "this level does not use a conveyor belt";
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
        return App.getGameSession().renderTileStatus(x, y);
    }

    public String zombiesInfo() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        return App.getGameSession().renderZombiesInfo();
    }

    public String cheatSpawnZombie(CheatSpawnZombieRequest request) {
        int row, col;
        try {
            col = Integer.parseInt(request.x());
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
        if (col < 0 || col >= lawn.getCols()) {
            return "col out of bounds (0-" + (lawn.getCols() - 1) + ")";
        }

        try {
            Zombie zombie = ZombieFactory.create(request.type(), row, col);
            session.spawnZombie(zombie);
            return request.type() + " spawned in row " + row + " at col " + col;
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
        GameSession session = App.getGameSession();
        if (session == null) return "no active game session";
        if (session.isWavesStarted()) return "waves already started";

        session.startWaves();

        Level level = session.getLevel();
        int totalWaves = (level != null && level.getWaves() != null) ? level.getWaves().size() : 0;
        return totalWaves > 0
                ? "zombie waves started (" + totalWaves + " waves loaded)"
                : "zombie waves started (no wave data — add waves to your level JSON)";
    }
}