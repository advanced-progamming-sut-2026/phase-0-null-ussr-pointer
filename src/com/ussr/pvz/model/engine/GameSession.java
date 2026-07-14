package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.event.GameEventBus;
import com.ussr.pvz.model.entities.items.CoinDrop;
import com.ussr.pvz.model.entities.items.DiamondDrop;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.entities.zombies.projectiles.ZombieProjectile;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.chaptereffect.ChapterEffect;
import com.ussr.pvz.model.level.chaptereffect.ChapterEffectRegistry;
import com.ussr.pvz.model.quest.QuestEventTracker;
import com.ussr.pvz.model.state.ResourceState;
import com.ussr.pvz.model.util.Vec2;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameSession {

    private static final int COIN_DROP_CHANCE_PERCENT = 15;
    private static final int DIAMOND_DROP_CHANCE_PERCENT = 2;
    private static final int DIAMOND_DROP_AMOUNT = 1;

    private final GameEventBus eventBus = new GameEventBus();
    private final java.util.Random lootRandom = new java.util.Random();

    private GameClock clock = new GameClock();
    private Level level;
    private ResourceState resourceState;
    private List<Zombie> zombies;
    private List<GroundItem> items;
    private List<Plant> plants;
    private int sunCount;
    private int plantFoodCount;
    private boolean wavesStarted;
    private Lawn lawn;
    private boolean gameOver = false;

    private static final int LAWN_COLS = 9;
    private List<LawnMower> lawnMowers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<ZombieProjectile> zombieProjectiles = new ArrayList<>();

    public void initClock() {
        clock.reset();
        plants.forEach(clock::addEntity);
        zombies.forEach(clock::addEntity);
        items.forEach(clock::addEntity);
        projectiles.forEach(clock::addEntity);
        initLawnMowers();
    }

    private void initLawnMowers() {
        if (lawn == null) return;
        lawnMowers.clear();

        int rows = lawn.getRows();
        for (int r = 0; r < rows; r++) {
            LawnMower mower = new LawnMower(r, new Vec2(-0.5, r));
            lawnMowers.add(mower);
            clock.addEntity(mower);
        }
    }

    public void tick() {
        // FIX: Freeze the entire simulation if the game has ended (Win or Loss)
        if (gameOver) {
            return;
        }

        clock.tick();
        // TODO: Sky Sun Generation
        //  1. Check if the level allows sky sun: if (level != null && level.isSunFalling())
        //  2. Increment skySunTimer += GameClock.SECONDS_PER_TICK;
        //  3. If timer >= SKY_SUN_INTERVAL, spawn a new SunToken, add it to the session, and reset timer.

        if (level != null) {
            ChapterEffect effect = ChapterEffectRegistry.get(level.getChapter());
            if (effect != null) {
                effect.onTick(this, level, GameClock.SECONDS_PER_TICK);
            }
        }

        // Delegate core tick progression directly to the behavior polymorphically
        if (level != null && level.getBehavior() != null) {
            level.getBehavior().tick(this, GameClock.SECONDS_PER_TICK);
        }

        plants.removeIf(p -> !p.isAlive());
        zombies.removeIf(z -> !z.isAlive());
        items.removeIf(i -> !i.isAlive());
        projectiles.removeIf(p -> !p.isAlive());
        lawnMowers.removeIf(m -> !m.isAlive());
        zombieProjectiles.removeIf(p -> !p.isAlive());
        cleanupDeadGridStructures();
        checkZombieBreaches();

        // Polymorphic loss condition check
        if (level != null && level.getBehavior() != null && level.getBehavior().isFailed(level)) {
            gameOver = true;
        }

        // Check for Win Condition
        if (areWavesDone()) {
            gameOver = true; // Freeze on win

            // TODO: Level Progression & Saving
            //  1. Call App.getLevelManager().nextLevel() to advance the campaign.
            //  2. Apply level-completion rewards to App.getAccount().getAdventureProgress().
            //  3. Convert all accounts to AccountState and call SaveService.saveAccounts() to persist progress to disk.

            // TODO: Exit Game State
            //  Force the player back to the Main Menu or a Post-Game Summary screen so they aren't trapped.
            //  App.setMenuState(MenuState.MAIN);
        }
    }

    private void cleanupDeadGridStructures() {
        if (lawn == null) return;

        int rows = lawn.getRows();
        int cols = lawn.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                var cell = lawn.getCell(r, c);
                if (cell == null || cell.getInteractableStructure() == null) continue;

                var structure = cell.getInteractableStructure();
                if (!structure.isAlive()) {
                    structure.onDestroy(this);
                    cell.setStructure(null);
                }
            }
        }
    }

    public void addZombieProjectile(com.ussr.pvz.model.entities.zombies.projectiles.ZombieProjectile zp) {
        zombieProjectiles.add(zp);
        clock.addEntity(zp);
    }

    public void registerStructure(InteractableStructure structure) {
        if (structure == null) return;
        clock.addEntity(structure);
    }

    private void checkZombieBreaches() {
        if (level == null || level.getBehavior() == null) return;

        for (Zombie zombie : zombies) {
            if (!zombie.isAlive()) continue;

            if (zombie.getPosition().x() < 0.0) {
                int row = (int) zombie.getPosition().y();
                LawnMower mower = getMowerForLane(row);

                if (mower != null && !mower.isActivated()) {
                    mower.activate();
                    eventBus.publish(new GameEvent.LawnMowerTriggered(row));
                    eventBus.publish(new GameEvent.ZombieBreachedLane(row));
                } else {
                    // Let the behavior safely decide how to handle breaches
                    level.getBehavior().onZombieBreach(this, zombie);
                    if (gameOver) break;
                }
            }
        }
    }

    private LawnMower getMowerForLane(int lane) {
        return lawnMowers.stream()
                .filter(m -> m.getLane() == lane)
                .findFirst()
                .orElse(null);
    }

    public void spawnZombie(Zombie zombie) {
        zombies.add(zombie);
        clock.addEntity(zombie);
        if (App.getAccount() != null) {
            List<String> seen = App.getAccount().getAdventureProgress().getSeenZombies();
            if (!seen.contains(zombie.getAlias())) {
                App.getAccount().getAdventureProgress().addSeenZombies(zombie.getAlias());
            }
        }
        eventBus.publish(new GameEvent.ZombieSpawned(
                zombie.getAlias(),
                (int) zombie.getPosition().y(),
                (int) zombie.getPosition().x()
        ));
    }

    public void onZombieReachedEnd() {
        gameOver = true;
        eventBus.publish(new GameEvent.ZombieReachedHouse(-1));
        eventBus.publish(new GameEvent.GameOver());
    }

    public void addPlant(Plant plant) {
        if (plant == null) return;
        plants.add(plant);
        clock.addEntity(plant);
        notifyPlantPlanted(plant);
    }

    public void notifyPlantFoodUsed(Plant plant) {
        eventBus.publish(new GameEvent.PlantFoodUsed(
                plant.getName(),
                plant.getLocation().y(),
                plant.getLocation().x()
        ));
    }

    public void notifyPlantDamaged(Plant plant, int damageDealt) {
        eventBus.publish(new GameEvent.PlantDamaged(
                plant.getName(),
                plant.getLocation().y(),
                plant.getLocation().x(),
                damageDealt,
                plant.getHp()
        ));

        if (!plant.isAlive()) {
            eventBus.publish(new GameEvent.PlantDied(
                    plant.getName(),
                    plant.getLocation().y(),
                    plant.getLocation().x()
            ));

            if (level != null && level.getBehavior() != null) {
                level.getBehavior().onPlantDied(this, plant);
            }
        }
    }

    public void notifyPlantPlanted(Plant plant) {
        eventBus.publish(new GameEvent.PlantPlanted(
                plant.getName(),
                plant.getLocation().y(),
                plant.getLocation().x()
        ));
    }

    public void notifyPlantPlucked(Plant plant) {
        eventBus.publish(new GameEvent.PlantPlucked(
                plant.getName(),
                plant.getLocation().y(),
                plant.getLocation().x()
        ));
    }

    // TODO: Ensure all Plant ShootStrategy logic calls this instead of session.getProjectiles().add()!
    // This fixes the systemic bug where player projectiles weren't getting ticked by the clock.
    public void addProjectile(Projectile projectile) {
        if (projectile == null) return;
        projectiles.add(projectile);
        clock.addEntity(projectile);
    }

    // FIX: Helper to safely spawn items so they get tracked by the GameClock
    public void addItem(GroundItem item) {
        if (item == null) return;
        items.add(item);
        clock.addEntity(item);
    }

    private void rollZombieLoot(Zombie zombie) {
        if (zombie.getPosition() == null) return;

        if (lootRandom.nextInt(100) < COIN_DROP_CHANCE_PERCENT) {
            CoinDrop.CoinTier tier = rollCoinTier();
            CoinDrop coinDrop = new CoinDrop(tier);
            coinDrop.setPosition(zombie.getPosition());
            addItem(coinDrop);
        }

        if (lootRandom.nextInt(100) < DIAMOND_DROP_CHANCE_PERCENT) {
            DiamondDrop diamondDrop = new DiamondDrop(DIAMOND_DROP_AMOUNT);
            diamondDrop.setPosition(zombie.getPosition());
            addItem(diamondDrop);
        }
    }

    private CoinDrop.CoinTier rollCoinTier() {
        int roll = lootRandom.nextInt(100);
        if (roll < 70) return CoinDrop.CoinTier.BRONZE;
        if (roll < 95) return CoinDrop.CoinTier.SILVER;
        return CoinDrop.CoinTier.GOLD;
    }

    public void notifyZombieDied(Zombie zombie, String killerPlantName) {
        eventBus.publish(new GameEvent.ZombieDied(
                zombie.getAlias(),
                zombie.getPosition().x(),
                zombie.getPosition().y(),
                killerPlantName
        ));

        rollZombieLoot(zombie);

        if (level != null && level.getBehavior() != null) {
            level.getBehavior().onZombieDied(this, zombie);
        }
    }

    public void notifyZombieDied(Zombie zombie) {
        notifyZombieDied(zombie, "Unknown");
    }

    public void notifyGraveDestroyed(int row, int col) {
        eventBus.publish(new GameEvent.GraveDestroyed(row, col));
    }

    public void notifyStructureDestroyed(String structureType, int row, int col) {
        eventBus.publish(new GameEvent.StructureDestroyed(structureType, row, col));
    }

    public int getSunCount() {
        return sunCount;
    }

    public void addSun(int amount) {
        sunCount += amount;
        if (level != null && level.getBehavior() != null) {
            level.getBehavior().onSunCollected(this, amount);
        }
    }

    public boolean spendSun(int amount) {
        if (sunCount < amount) return false;
        sunCount -= amount;
        return true;
    }

    public void addPlantFood() {
        plantFoodCount++;
    }

    public boolean spendPlantFood() {
        if (plantFoodCount <= 0) return false;
        plantFoodCount--;
        return true;
    }

    public void killAllZombies() {
        if (zombies != null) {
            zombies.forEach(z -> z.isAlive = false);
            zombies.clear();
        }
    }

    public void removeAllCooldowns() {
        if (App.getAccount() != null && App.getAccount().getCollection() != null) {
            for (Plant plant : App.getAccount().getCollection().unlockedPlants()) {
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

    public String renderMap() {
        if (lawn == null) return "map not initialized";
        StringBuilder sb = new StringBuilder();
        int rows = lawn.getRows();
        int cols = lawn.getCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                var cell = lawn.getCell(r, c);
                if (cell == null) {
                    sb.append(".");
                } else if (cell.getPlant() != null) {
                    sb.append("P");
                } else if (cell.getInteractableStructure() instanceof com.ussr.pvz.model.board.structures.Grave) {
                    sb.append("G");
                } else {
                    sb.append(".");
                }
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    public String renderPlantsStatus() {
        if (plants == null || plants.isEmpty()) return "no plants on the field";
        StringBuilder sb = new StringBuilder();
        for (Plant plant : plants) {
            sb.append(plant.getName())
                    .append(" | hp: ").append(plant.getHp())
                    .append(" | level: ").append(plant.getLevel())
                    .append("\n");
        }
        return sb.toString().trim();
    }

    public String renderTileStatus(int row, int col) {
        if (lawn == null) return "map not initialized";
        var cell = lawn.getCell(row, col);
        if (cell == null) return "invalid tile (" + row + ", " + col + ")";
        StringBuilder sb = new StringBuilder();
        sb.append("tile (").append(row).append(", ").append(col).append("): ");

        if (cell.getPlant() != null) {
            sb.append("plant=").append(cell.getPlant().getName())
                    .append(" hp=").append(cell.getPlant().getHp());
        } else if (cell.getInteractableStructure() instanceof com.ussr.pvz.model.board.structures.Grave grave) {
            sb.append("structure=Grave hp=").append(grave.getHp());
        } else {
            sb.append("empty");
        }
        return sb.toString();
    }

    public String renderZombiesInfo() {
        if (zombies == null || zombies.isEmpty()) return "no zombies on the field";
        StringBuilder sb = new StringBuilder();
        for (Zombie zombie : zombies) {
            sb.append(zombie.toString()).append("\n");
        }
        return sb.toString().trim();
    }

    public boolean removePlantAt(int x, int y) {
        Plant.Location targetLoc = new Plant.Location(x, y);

        Optional<Plant> plantOpt = plants.stream()
                .filter(p -> p.isAlive() && targetLoc.equals(p.getLocation()))
                .findFirst();

        if (plantOpt.isEmpty()) return false;

        Plant plant = plantOpt.get();
        plant.setAlive(false);
        plants.remove(plant);
        notifyPlantPlucked(plant);

        if (lawn != null) {
            var cell = lawn.getCell(y, x);
            if (cell != null && cell.getPlant() == plant) {
                cell.setPlant(null);
            }
        }

        return true;
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
        return clock.getElapsedSeconds();
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
}