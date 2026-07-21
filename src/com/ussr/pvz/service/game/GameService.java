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
import com.ussr.pvz.model.quest.QuestRewardApplier;
import com.ussr.pvz.model.quest.QuestType;

public class GameService {

    public String menuEnterChapter(MenuEnterChapterRequest request) {
        //TODO : in plant plant method some plants like grave buster should can be planted on graves fix it
        String chapterId = request.chapterName();

        if (App.getGameSession() != null && !App.getGameSession().isGameOver()) {
            return "Cannot enter a chapter mid-game. Quit or finish the current game first.";
        }

        if (chapterId == null) {
            return "chapter name cannot be null.";
        }

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
        if (account == null || account.getAdventureProgress() == null) {
            return "coin balance: 0 (no active session)";
        }
        return "coin balance: " + account.getAdventureProgress().getCoin();
    }

    public String menuGemWallet() {
        Account account = App.getAccount();
        if (account == null || account.getAdventureProgress() == null) {
            return "gem balance: 0 (no active session)";
        }
        return "gem balance: " + account.getAdventureProgress().getGem();
    }

    public String menuSwitchWorld(MenuSwitchWorldRequest request) {
        String worldId = request.worldName();

        if (worldId == null) {
            return "world name cannot be null.";
        }

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
        if (App.getGameSession() == null) {
            return "no active game session";
        }

        int x, y;
        try {
            x = Integer.parseInt(request.x());
            y = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "invalid location";
        }

        var matchingSun = App.getGameSession().getItems().stream()
                .filter(item -> item != null && item.getItemType() == ItemType.SUN)
                .filter(item -> !item.isCollected())
                .filter(item -> item.getLocation() != null && item.getLocation().equals(new GroundItem.Location(x, y)))
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
        App.getGameSession().addSun(count);
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
        if (App.getGameSession() == null) {
            return "no active game session";
        }

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
        //todo : we should check the plant that if it exist in the list of plants we have chosen in the add plant not the plants in the account
        int x, y;
        try {
            x = Integer.parseInt(request.x());
            y = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "invalid location";
        }

        try {
            GameSession session = requireSession();
            Plant blueprint;
            try {
                blueprint = requireUnlockedPlant(request.type());
            } catch (Exception e) {
                return "Invalid or unknown plant type: " + request.type();
            }

            if (blueprint == null) {
                throw new IllegalStateException("Failed to load a valid structural configuration template for " + request.type());
            }

            Cell cell = requirePlantableCell(session, x, y, blueprint);

            if (blueprint.getRecharge() > 0) {
                throw new IllegalStateException("Plant is refreshing! Please wait.");
            }

            if (!session.spendSun(blueprint.getCost())) {
                throw new IllegalStateException(
                        "not enough sun to plant " + (blueprint.getName() != null ? blueprint.getName() : "this plant") + " (needs " + blueprint.getCost() + ")");
            }

            Plant plant = instantiatePlant(blueprint, x, y);

            if (App.getAccount() != null && App.getAccount().getSavedBoosts() != null && blueprint.getName() != null) {
                if (App.getAccount().getSavedBoosts().useBoost(blueprint.getName())) {
                    if (plant.getPlantFoodType() != null && plant.getPlantFoodType() != PlantFoodType.NONE && plant.getPlantFoodEffect() != null) {
                        plant.getPlantFoodEffect().triggerSuperpower(plant, session);
                        plant.getPlantFoodEffect().applyStatusModifiers(plant);
                        session.notifyPlantFoodUsed(plant);
                    }
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
        } catch (Exception e) {
            // CRITICAL GLOVE: Intercept any unexpected NullPointerException or runtime anomalies
            // to print the diagnostic trace while preventing the game process from crashing.
            System.err.println("CRITICAL ERROR inside GameService.plantPlant:");
            e.printStackTrace();
            return "An internal system error occurred while planting: " + e.getMessage();
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
        if (blueprint == null) {
            throw new IllegalStateException("Plant profile template is completely missing.");
        }

        Lawn lawn = session.getLawn();
        if (lawn == null || y < 0 || y >= lawn.getRows() || x < 0 || x >= lawn.getCols()) {
            throw new IllegalStateException("invalid location");
        }

        Cell cell = lawn.getCell(y, x);
        if (cell == null) {
            throw new IllegalStateException("invalid location");
        }

        boolean isWaterTile = cell.getTile() != null && cell.getTile().getType() == com.ussr.pvz.model.board.terrain.TileType.Water;

        // Null-safe Tag & Name evaluations
        boolean isAquaticPlant = false;
        if (blueprint.getTags() != null) {
            isAquaticPlant = blueprint.getTags().contains(com.ussr.pvz.model.entities.plants.Tag.WATER);
        }
        if (blueprint.getName() != null) {
            isAquaticPlant = isAquaticPlant ||
                    blueprint.getName().equalsIgnoreCase("Lily Pad") ||
                    blueprint.getName().equalsIgnoreCase("LilyPad");
        }

        boolean hasLilyPadUnderneath = false;
        if (!cell.isEmpty()) {
            Plant existingPlant = cell.getPlant();
            if (existingPlant != null && existingPlant.getName() != null) {
                hasLilyPadUnderneath = existingPlant.getName().equalsIgnoreCase("Lily Pad") ||
                        existingPlant.getName().equalsIgnoreCase("LilyPad");
            }
        }

        String displayName = blueprint.getName() != null ? blueprint.getName() : "this plant";

        if (cell.getTile() != null && !cell.getTile().allowsPlant()) {
            if (!(isWaterTile && (isAquaticPlant || hasLilyPadUnderneath))) {
                throw new IllegalStateException("cannot plant " + displayName + " on this tile (" + x + ", " + y + ")");
            }
        } else if (isWaterTile && !isAquaticPlant && cell.isEmpty()) {
            throw new IllegalStateException(displayName + " must be planted on a Lily Pad!");
        }

        if(cell.getPlant() != null) {
            throw new IllegalStateException("can not plant plant here cause there is an interactible structure here");
        }

        if (!cell.isEmpty()) {
            if (hasLilyPadUnderneath && !isAquaticPlant) {
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
        if (account.getAdventureProgress() == null) {
            throw new IllegalStateException("no profile progress found on current account");
        }

        String plantKey = requestedType == null ? "" : requestedType.trim().toUpperCase().replaceAll("[\\s_]", "");

        // Safe retrieve without raw unboxing side effects
        var progressMap = account.getAdventureProgress().getPlantLvls();
        if (progressMap == null) {
            throw new IllegalStateException("Could not access account unlock history.");
        }

        Integer level = progressMap.get(plantKey);
        if (level == null || level == 0) {
            throw new IllegalStateException("You haven't unlocked " + requestedType);
        }

        int plantId = PlantFactory.findIdByName(requestedType);
        Plant blueprint = PlantFactory.createPlant(plantId, level);
        if (blueprint == null) {
            throw new IllegalStateException("Database query error: template for ID " + plantId + " was not resolved by Factory.");
        }
        return blueprint;
    }

    private Plant instantiatePlant(Plant blueprint, int x, int y) {
        if (blueprint == null) {
            throw new IllegalArgumentException("Cannot instantiate a null blueprint copy.");
        }
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

            if (plant.getPlantFoodEffect() != null) {
                plant.getPlantFoodEffect().triggerSuperpower(plant, session);
                plant.getPlantFoodEffect().applyStatusModifiers(plant);
            }
            session.notifyPlantFoodUsed(plant);

            return "plant fed at (" + x + ", " + y + ")";
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            System.err.println("Exception inside GameService.feedPlant:");
            e.printStackTrace();
            return "Failed to feed plant: " + e.getMessage();
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
            throw new IllegalStateException((plant.getName() != null ? plant.getName() : "this plant") + " has no plant food effect");
        }
        if (plant.getPlantFoodTimer() > 0) {
            throw new IllegalStateException((plant.getName() != null ? plant.getName() : "this plant") + " is already under a plant food effect");
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
            if (conveyor.getConveyorBelt() == null || conveyor.getConveyorBelt().isEmpty()) {
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
        if (lawn == null) {
            return "map/lawn has not been initialized yet.";
        }
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

    public String cheatDecreaseHealth(String type, int amount) {
        GameSession session = App.getGameSession();
        if (session == null) return "no active game session";
        int count = 0;
        for (Zombie z : session.getZombies()) {
            if (z.isAlive() && z.getAlias().equalsIgnoreCase(type.trim())) {
                z.takeDamage(amount);
                count++;
            }
        }
        return "Decreased health of " + count + " " + type + " zombies by " + amount;
    }

    public String cheatUnlockAll() {
        Account account = App.getAccount();
        if (account == null) return "no active account";
        account.getAdventureProgress().setCurrentLvl(999);
        account.getAdventureProgress().setCurrentChapter(999);
        for (java.util.Map<String, Object> p : App.getCachedPlantsData()) {
            String name = p.get("name").toString().toUpperCase().replaceAll("[\\s_\\-]", "");
            account.getAdventureProgress().getPlantLvls().put(name, 1);
        }
        return "all chapters and plants unlocked";
    }

    public String cheatAddCurrency(CheatAddCurrencyRequest request) {
        int amount;
        try {
            amount = Integer.parseInt(request.amount());
        } catch (NumberFormatException e) {
            return "invalid amount";
        }
        Account account = App.getAccount();
        if (account == null || account.getAdventureProgress() == null) {
            return "no active profile account matched to receive currency.";
        }
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

    public String cheatCompleteQuest(String questId) {
        Account account = App.getAccount();
        if (account == null) {
            return "no account logged in";
        }

        var allQuests = account.getQuestManager().getByType(QuestType.DAILY);
        allQuests.addAll(account.getQuestManager().getByType(QuestType.CHALLENGE));
        allQuests.addAll(account.getQuestManager().getByType(QuestType.EPIC));

        var matchingQuest = allQuests.stream()
                .filter(q -> q.getId().equalsIgnoreCase(questId))
                .findFirst();

        if (matchingQuest.isEmpty()) {
            return "quest not found: " + questId + " | Available quest types: DAILY, CHALLENGE, EPIC";
        }

        var quest = matchingQuest.get();

        if (quest.isCompleted()) {
            return "quest already completed: " + quest.getTitle();
        }

        for (var criterion : quest.getCriteria()) {
            for (int i = 0; i < criterion.getTarget(); i++) {
                criterion.increment(1);
            }
        }

        // Mark quest as completed
        quest.setCompleted(true);

        // Apply reward automatically
        QuestRewardApplier.applyReward(quest.getReward(), quest.getTitle());

        return "quest completed: " + quest.getTitle() + " | Reward: " + quest.getReward().amount() + " " + quest.getReward().rewardType();
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
      public String showPlantFood() {
        if (App.getGameSession() == null) {
            return "no active game session";
        }
        return "Plant foods available: " + App.getGameSession().getPlantFoodCount();
    }
}