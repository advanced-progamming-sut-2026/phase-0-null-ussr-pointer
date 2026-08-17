package com.ussr.pvz.model.engine.session;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.shared.account.AccountState;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.NewsObserver;
import com.ussr.pvz.model.engine.Tickable;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.event.GameEventBus;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.projectiles.ZombieProjectile;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.level.GameMode;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.chaptereffect.ChapterEffect;
import com.ussr.pvz.model.level.chaptereffect.ChapterEffectRegistry;
import com.ussr.pvz.model.quest.QuestEventTracker;
import com.ussr.pvz.model.util.Vec2;
import com.ussr.pvz.server.account.SaveService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameSession {
    private boolean progressTracked = true;
    private double skySunTimer = 0.0;
    private boolean outroShown = false;

    public boolean isOutroShown()      { return outroShown; }
    public void    markOutroShown()    { outroShown = true; }
    private static final int LEVEL_COMPLETE_COIN_REWARD = 1000;

    private final GameEventBus eventBus = new GameEventBus();

    private Level level;
    private List<Zombie> zombies;
    private List<GroundItem> items;
    private List<Plant> plants;
    private final SessionResources resources =
            new SessionResources();
    private final SessionEventPublisher events =
            new SessionEventPublisher(eventBus);
    private final SessionRewardHandler rewards =
            new SessionRewardHandler(this);
    private final SessionUpdater updater =
            new SessionUpdater(
                    this,
                    this::applyReward
            );
    private boolean wavesStarted;
    private Lawn lawn;
    private boolean gameOver = false;
    private GameOutcome outcome = GameOutcome.IN_PROGRESS;
    private boolean levelIntroShown;
    private List<String> selectedPlants = new ArrayList<>();
    private final List<LawnMower> lawnMowers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<ZombieProjectile> zombieProjectiles = new ArrayList<>();
    private final List<BurningTile> burningTiles = new ArrayList<>();

    private List<String> boostedPlants = new ArrayList<>();

    public GameSession() {
        eventBus.subscribe(
                GameEvent.GameOver.class,
                event -> markDefeat()
        );
    }

    /**
     * Tracks a tile that was set to {@link TileType#Burning} temporarily
     * (e.g. by the Dragon Zomboss's row-ignite move) so it can be reverted
     * back to {@link TileType#Normal} once its timer runs out.
     */
    private static final class BurningTile {
        final int row;
        final int col;
        float remaining;

        BurningTile(int row, int col, float remaining) {
            this.row = row;
            this.col = col;
            this.remaining = remaining;
        }
    }

    public void initClock() {
        updater.resetClock();

        plants.forEach(updater::registerEntity);
        zombies.forEach(updater::registerEntity);
        items.forEach(updater::registerEntity);
        projectiles.forEach(updater::registerEntity);

        initLawnMowers();
    }

    private void initLawnMowers() {
        if (lawn == null) return;
        lawnMowers.clear();

        int rows = lawn.getRows();
        for (int r = 0; r < rows; r++) {
            LawnMower mower = new LawnMower(r, new Vec2(-0.5, r));
            lawnMowers.add(mower);
            updater.registerEntity(mower);
        }
    }

    public void update(float delta) {
        updater.update(delta);
    }

    private void applyReward(){
        if (this.progressTracked) {
            Account account = App.getAccount();
            boolean firstClear = account != null
                    && level != null
                    && level.getId() != null
                    && !account.getAdventureProgress().isLevelCompleted(level.getId());

            App.getLevelManager().completeCurrentLevel();
            if (firstClear) {
                account.getAdventureProgress().addCompletedLevel(level.getId());
                account.getAdventureProgress().addCoin(LEVEL_COMPLETE_COIN_REWARD);

                for (String plantAlias : level.getRewardPlantAliases()) {
                    account.getAdventureProgress().upgradePlant(plantAlias);
                }

                if (!level.getRewardPlantAliases().isEmpty()) {
                    NewsObserver.triggerNewPlant(level.getRewardPlantAliases());
                }
                NewsObserver.triggerNewLevel(level);
            }

            try {
                App.getLevelManager().nextLevel();
            } catch (IllegalStateException e) {
                System.err.println("[GameSession] Could not advance to next level: " + e.getMessage());
            }
            com.ussr.pvz.model.level.Chapter completedChapter = level == null
                    ? null
                    : App.getLevelManager().findChapter(level.getChapter());
            if (firstClear && completedChapter != null
                    && completedChapter.getGameMode().equals(GameMode.MINIGAME)) {
                NewsObserver.triggerNewMiniGame(this.level);
            }

            List<AccountState> updatedStates = App.getAccounts().stream()
                    .map(Account::toState)
                    .toList();
            SaveService.saveAccounts(updatedStates);
        }
    }

    public void addZombieProjectile(
            ZombieProjectile projectile
    ) {
        zombieProjectiles.add(projectile);
        updater.registerEntity(projectile);
    }

    public void registerStructure(
            InteractableStructure structure
    ) {
        if (structure != null) {
            updater.registerEntity(structure);
        }
    }

    public void registerTickable(Tickable tickable) {
        if (tickable != null) {
                updater.registerEntity(tickable);
            }
        }

    public void spawnZombie(Zombie zombie) {
        if (!zombie.isGlowing() && Math.random() < 0.05) {
            zombie.setGlowing(true);
        }

        zombies.add(zombie);
        updater.registerEntity(zombie);
        if (App.getAccount() != null) {
            List<String> seen = App.getAccount().getAdventureProgress().getSeenZombies();
            if (!seen.contains(zombie.getAlias())) {
                App.getAccount().getAdventureProgress().addSeenZombies(zombie.getAlias());
                NewsObserver.triggerNewZombie(zombie);
            }
        }
        eventBus.publish(new GameEvent.ZombieSpawned(
                zombie.getAlias(),
                (int) zombie.getPosition().y(),
                (int) zombie.getPosition().x(),
                zombie.isGlowing()
        ));
    }

    public void onZombieReachedEnd() {
        if (gameOver) return;

        eventBus.publish(new GameEvent.ZombieReachedHouse(-1));
        concludeDefeat();
    }

    public void addPlant(Plant plant) {
        if (plant == null) return;
        plants.add(plant);
        updater.registerEntity(plant);
        notifyPlantPlanted(plant);
    }

    public void notifyPlantFoodUsed(Plant plant) {
        events.plantFoodUsed(plant);
    }

    public void notifyPlantPlanted(Plant plant) {
        events.plantPlanted(plant);
    }

    public void notifyPlantPlucked(Plant plant) {
        events.plantPlucked(plant);
    }

    public void addProjectile(Projectile projectile) {
        if (projectile == null) return;
        projectiles.add(projectile);
        updater.registerEntity(projectile);
    }

    public void addItem(GroundItem item) {
        if (item == null) return;
        items.add(item);
        updater.registerEntity(item);
    }

    public void notifyZombieDied(Zombie zombie, String killerPlantName) {
        events.zombieDied(zombie, killerPlantName);

        rewards.rollZombieLoot(zombie);

        if (zombie.isGlowing()) {
            if (resources.getPlantFood() < 3) {
                resources.addPlantFood();
            }

            eventBus.publish(
                    new GameEvent.GlowingZombieDroppedPlantFood(
                            resources.getPlantFood()
                    )
            );
        }

        if (level != null && level.getBehavior() != null) {
            level.getBehavior().onZombieDied(this, zombie);
        }
    }

    public void notifyZombieDied(Zombie zombie) {
        notifyZombieDied(zombie, "Unknown");
    }

    public void notifyGraveDestroyed(
            int row,
            int column
    ) {
        events.graveDestroyed(row, column);
    }

    public void notifyStructureDestroyed(
            String structureType,
            int row,
            int col
    ) {
        events.structureDestroyed(
                structureType,
                row,
                col
        );
    }

    public int getSunCount() {
        return resources.getSun();
    }

    public void addSun(int amount) {
        resources.addSun(amount);

        if (level != null
                && level.getBehavior() != null) {
            level.getBehavior()
                    .onSunCollected(this, amount);
        }
    }

    public boolean spendSun(int amount) {
        return resources.spendSun(amount);
    }

    public void addPlantFood() {
        resources.addPlantFood();
    }

    public boolean spendPlantFood() {
        return resources.spendPlantFood();
    }

    public int getPlantFoodCount() {
        return resources.getPlantFood();
    }

    public void killAllZombies() {
        if (zombies != null) {
            zombies.forEach(z -> z.setAlive(false));
            zombies.clear();
        }
    }

    public void removeAllCooldowns() {
        if (App.getAccount() != null) {
            for (Plant plant : App.getAccount().getAdventureProgress().getAccountPlants()) {
                plant.setRecharge(0);
            }
        }
    }

    public void startWaves() {
        if (App.getAccount() != null) {
            QuestEventTracker tracker = new QuestEventTracker(App.getAccount().getQuestManager());
            tracker.subscribeTo(this);
        }

        wavesStarted = true;
    }

    public boolean isWavesStarted() {
        return wavesStarted;
    }

    public boolean areWavesDone() {
        if (!wavesStarted || level == null || level.getBehavior() == null) return false;

        var ai = level.getBehavior().getAiManager();
        if (ai == null) {
            return zombies.isEmpty();
        }

        return ai.areAllWavesDone(level.getWaves()) && zombies.isEmpty();
    }

    public boolean removePlantAt(int x, int y) {
        if (lawn == null) {
            return false;
        }

        Cell cell = lawn.getCell(y, x);

        if (cell == null || cell.getPlant() == null) {
            return false;
        }

        Plant plant = cell.getPlant();
        Plant bottomPlant = plant.getBottom();

        plant.setAlive(false);
        plant.setBottom(null);

        plants.remove(plant);
        notifyPlantPlucked(plant);

        if (bottomPlant != null && bottomPlant.isAlive()) {
            cell.setPlant(bottomPlant);
        } else {
            cell.setPlant(null);
        }

        return true;
    }

    public void igniteTileTemporarily(int row, int col, float durationSeconds) {
        if (lawn == null || durationSeconds <= 0f) {
            return;
        }

        Tile tile = lawn.getTile(row, col);
        if (tile == null) {
            return;
        }

        tile.setType(TileType.Burning);

        for (BurningTile burning : burningTiles) {
            if (burning.row == row && burning.col == col) {
                burning.remaining = durationSeconds;
                return;
            }
        }

        burningTiles.add(new BurningTile(row, col, durationSeconds));
    }

    public void tickBurningTiles(float delta) {
        if (burningTiles.isEmpty() || lawn == null) {
            return;
        }

        Iterator<BurningTile> iterator = burningTiles.iterator();
        while (iterator.hasNext()) {
            BurningTile burning = iterator.next();
            burning.remaining -= delta;

            if (burning.remaining <= 0f) {
                Tile tile = lawn.getTile(burning.row, burning.col);
                if (tile != null && tile.getType() == TileType.Burning) {
                    tile.setType(TileType.Normal);
                }
                iterator.remove();
            }
        }
    }

    public void triggerWaveStart(int waveNumber, boolean isFinalWave) {
        eventBus.publish(new GameEvent.WaveStarted(waveNumber, isFinalWave));

        if (level != null) {
            ChapterEffect effect = ChapterEffectRegistry.get(level.getChapter());
            if (effect != null) {
                effect.onWaveStart(this, level, waveNumber, isFinalWave);
            }
        }
    }

    public GameEventBus getEventBus() {
        return eventBus;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if (!gameOver) {
            outcome = GameOutcome.IN_PROGRESS;
        }
    }

    public GameOutcome getOutcome() {
        return outcome;
    }

    public boolean isVictory() {
        return outcome == GameOutcome.VICTORY;
    }

    public boolean isLevelIntroShown() {
        return levelIntroShown;
    }

    public void markLevelIntroShown() {
        levelIntroShown = true;
    }

    public void concludeVictory() {
        if (gameOver) {
            return;
        }
        outcome = GameOutcome.VICTORY;
        gameOver = true;
        eventBus.publish(new GameEvent.GameWon());
    }

    public void concludeDefeat() {
        if (gameOver) {
            return;
        }
        markDefeat();
        eventBus.publish(new GameEvent.GameOver());
    }

    private void markDefeat() {
        if (outcome == GameOutcome.VICTORY) {
            return;
        }
        outcome = GameOutcome.DEFEAT;
        gameOver = true;
    }

    public Lawn getLawn() {
        return lawn;
    }

    public void setLawn(Lawn lawn) {
        this.lawn = lawn;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    public void setZombies(List<Zombie> zombies) {
        this.zombies = zombies;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
        this.skySunTimer = 0.0;
        if (level != null) {
            ChapterEffect effect = ChapterEffectRegistry.get(level.getChapter());
            if (effect != null) {
                effect.onStart(this, level);
            }
        }
    }

    public List<GroundItem> getItems() {
        return items;
    }

    public void setItems(List<GroundItem> items) {
        this.items = items;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public double getElapsedSeconds() {
        return updater.getElapsedSeconds();
    }

    public void notifyPlantDied(Plant plant) {
        eventBus.publish(new GameEvent.PlantDied(
                plant.getName(),
                plant.getLocation().y(),
                plant.getLocation().x()
        ));

        if (level != null && level.getBehavior() != null) {
            level.getBehavior().onPlantDied(this, plant);
        }
    }

    public void setProgressTracked(boolean progressTracked) {
        this.progressTracked = progressTracked;
    }
    public List<LawnMower> getLawnMowers() {
        return lawnMowers;
    }

    public List<String> getSelectedPlants() {
        return selectedPlants;
    }

    public void setSelectedPlants(List<String> selectedPlants) {
        this.selectedPlants = selectedPlants;
    }

    public List<String> getBoostedPlants() { return this.boostedPlants; }

    public void setBoostedPlants(List<String> boostedPlants) { this.boostedPlants = boostedPlants; }

    public List<ZombieProjectile> getZombieProjectiles() {
        return zombieProjectiles;
    }
}