package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.event.GameEventBus;
import com.ussr.pvz.model.entities.items.CoinDrop;
import com.ussr.pvz.model.entities.items.DiamondDrop;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.sun.SunToken;
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
import com.ussr.pvz.service.SaveService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameSession {
    private static final int COIN_DROP_CHANCE_PERCENT = 15;
    private static final int DIAMOND_DROP_CHANCE_PERCENT = 2;
    private static final int DIAMOND_DROP_AMOUNT = 1;
    private boolean progressTracked = true;
    private double skySunTimer = 0.0;

    private static final int LEVEL_COMPLETE_COIN_REWARD = 1000;

    private final GameEventBus eventBus = new GameEventBus();
    private final java.util.Random lootRandom = new java.util.Random();

    private final GameClock clock = new GameClock();
    private Level level;
    private List<Zombie> zombies;
    private List<GroundItem> items;
    private List<Plant> plants;
    private int sunCount;
    private int plantFoodCount;
    private boolean wavesStarted;
    private Lawn lawn;
    private boolean gameOver = false;
    private List<String> selectedPlants = new ArrayList<>();
    private final List<LawnMower> lawnMowers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<ZombieProjectile> zombieProjectiles = new ArrayList<>();

    private List<String> boostedPlants = new ArrayList<>();

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
        if (gameOver) {
            return;
        }

        clock.tick();
        if (level != null && level.isSunFalling() && lawn != null) {
            double elapsed = clock.getElapsedSeconds();
            double baseInterval = Math.max(6.0, 12.0 - 0.05 * elapsed);

            int diff = App.getAccount() != null ? App.getAccount().getDifficultyLvl() : 3;
            double diffMultiplier = diff / 3.0;
            double actualInterval = baseInterval * diffMultiplier;

            skySunTimer += GameClock.SECONDS_PER_TICK;
            if (skySunTimer >= actualInterval) {
                addItem(new SunToken(lawn.getRows(), lawn.getCols()));
                skySunTimer = 0.0;
            }
        }

        if (level != null) {
            ChapterEffect effect = ChapterEffectRegistry.get(level.getChapter());
            if (effect != null) {
                effect.onTick(this, level, GameClock.SECONDS_PER_TICK);
            }
        }
        if (level != null && level.getBehavior() != null) {
            level.getBehavior().tick(this, GameClock.SECONDS_PER_TICK);
        }

        if (App.getAccount() != null) {
            for (Plant p : App.getAccount().getAdventureProgress().getAccountPlants()) {
                p.tickRecharge(GameClock.SECONDS_PER_TICK);
            }
        }

        for (Plant p : plants) {
            if (!p.isAlive() && lawn != null && p.getLocation() != null) {
                Cell cell = lawn.getCell(p.getLocation().y(), p.getLocation().x());
                if (cell != null && cell.getPlant() == p) {
                    cell.setPlant(null);
                }
                notifyPlantDied(p);
            }
        }

        plants.removeIf(p -> !p.isAlive());
        zombies.removeIf(z -> !z.isAlive());
        items.removeIf(i -> !i.isAlive());
        projectiles.removeIf(p -> !p.isAlive());
        lawnMowers.removeIf(m -> !m.isAlive());
        zombieProjectiles.removeIf(p -> !p.isAlive());
        cleanupDeadGridStructures();
        checkZombieBreaches();

        if (!gameOver && level != null && level.getBehavior() != null && level.getBehavior().isFailed(level)) {
            gameOver = true;
            App.setMenuState(MenuState.MAIN);
        }

        if (!gameOver && areWavesDone()) {
            gameOver = true;

            if (this.progressTracked) {
                Account account = App.getAccount();
                App.getLevelManager().completeCurrentLevel();
                if (account != null) {
                    account.getAdventureProgress().addCoin(LEVEL_COMPLETE_COIN_REWARD);

                    for (String plantAlias : level.getRewardPlantAliases()) {
                        account.getAdventureProgress().upgradePlant(plantAlias);
                        NewsObserver.triggerNewPlant(level.getRewardPlantAliases());
                    }
                }

                try {
                    App.getLevelManager().nextLevel();
                } catch (IllegalStateException e) {
                    System.err.println("[GameSession] Could not advance to next level: " + e.getMessage());
                }
                if (App.getLevelManager().getCurrentChapter().getGameMode().equals(GameMode.MINIGAME)) {
                    NewsObserver.triggerNewMiniGame(this.level);
                }

                List<AccountState> updatedStates = App.getAccounts().stream()
                        .map(Account::toState)
                        .toList();
                SaveService.saveAccounts(updatedStates);
            }
            App.setMenuState(MenuState.GAME);
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
            if (gameOver) break;

            if (!zombie.isAlive()) continue;

            double zx = zombie.getPosition().x();
            int row = (int) zombie.getPosition().y();

            if (zx < 0.0) {
                LawnMower mower = getMowerForLane(row);
                if (mower != null && !mower.isActivated()) {
                    mower.activate();
                    eventBus.publish(new GameEvent.LawnMowerTriggered(row));
                    eventBus.publish(new GameEvent.ZombieBreachedLane(row));
                }
            }

            if (zx < -1.5) {
                level.getBehavior().onZombieBreach(this, zombie);
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
        if (!zombie.isGlowing() && Math.random() < 0.05) {
            zombie.setGlowing(true);
        }

        zombies.add(zombie);
        clock.addEntity(zombie);
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

        gameOver = true;
        eventBus.publish(new GameEvent.ZombieReachedHouse(-1));
        eventBus.publish(new GameEvent.GameOver());
        App.setMenuState(MenuState.MAIN);
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

    public void addProjectile(Projectile projectile) {
        if (projectile == null) return;
        projectiles.add(projectile);
        clock.addEntity(projectile);
    }

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

        if (zombie.isGlowing()) {
            if (plantFoodCount < 3) {
                plantFoodCount++;
            }
            eventBus.publish(new GameEvent.GlowingZombieDroppedPlantFood(plantFoodCount));
        }

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

    public int getPlantFoodCount() {
        return plantFoodCount;
    }

    public void killAllZombies() {
        if (zombies != null) {
            zombies.forEach(z -> z.isAlive = false);
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

    public String renderMap() {
        if (lawn == null) return "map not initialized";
        StringBuilder sb = new StringBuilder();
        int rows = lawn.getRows();
        int cols = lawn.getCols();

        for (int r = 0; r < rows; r++) {
            StringBuilder tileSide = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                var cell = lawn.getCell(r, c);
                if (cell == null || cell.getTile() == null) {
                    tileSide.append('?');
                } else {
                    tileSide.append(tileSymbol(cell.getTile().getType()));
                }
            }

            StringBuilder leftSide = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                var cell = lawn.getCell(r, c);

                boolean hasSun = false;
                boolean hasSeedPack = false;
                if (items != null) {
                    for (GroundItem item : items) {
                        if (item.isAlive() && item.getItemType() == ItemType.SUN && item.getLocation() != null) {
                            if (item.getLocation().x() == c && item.getLocation().y() == r) {
                                hasSun = true;
                                break;
                            }
                        }
                        if (item.isAlive && item.getItemType() == ItemType.SEED_PACK && item.getLocation() != null) {
                            if (item.getLocation().x() == c && item.getLocation().y() == r) {
                                hasSeedPack = true;
                                break;
                            }
                        }
                    }
                }

                if (cell == null) {
                    if (hasSun)
                        leftSide.append('*');
                    else if (hasSeedPack)
                        leftSide.append('@');
                    else
                        leftSide.append('.');
                } else if (cell.getPlant() != null) {
                    leftSide.append("P");
                } else if (cell.getInteractableStructure() instanceof com.ussr.pvz.model.board.structures.Grave grave) {
                    if (grave.getContent() == com.ussr.pvz.model.board.structures.Grave.Content.SUN) {
                        leftSide.append("S");
                    } else if (grave.getContent() == com.ussr.pvz.model.board.structures.Grave.Content.PLANT_FOOD) {
                        leftSide.append("F");
                    } else {
                        leftSide.append("G");
                    }
                } else if (cell.getInteractableStructure() != null) {
                    if (cell.getInteractableStructure() instanceof Vase) {
                        leftSide.append("V");
                    } else
                        leftSide.append("I");
                } else if (hasSun) {
                    leftSide.append("*");
                } else {
                    leftSide.append(".");
                }
            }

            StringBuilder rightSide = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                int zCount = 0;
                if (zombies != null) {
                    for (Zombie z : zombies) {
                        if (z.isAlive() && z.getPosition() != null) {
                            int zRow = (int) z.getPosition().y();
                            int zCol = (int) Math.floor(z.getPosition().x());
                            if (zRow == r && zCol == c) {
                                zCount++;
                            }
                        }
                    }
                }
                rightSide.append(zCount == 0 ? "." : Math.min(zCount, 9));
            }

            StringBuilder projSide = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                int pCount = 0;

                for (Projectile p : projectiles) {
                    if (p.isAlive() && p.getPosition() != null) {
                        int pRow = (int) p.getPosition().y();
                        int pCol = (int) Math.floor(p.getPosition().x());
                        if (pRow == r && pCol == c) {
                            pCount++;
                        }
                    }
                }

                for (ZombieProjectile zp : zombieProjectiles) {
                    if (zp.isAlive() && zp.getPosition() != null) {
                        int zpRow = (int) zp.getPosition().y();
                        int zpCol = (int) Math.floor(zp.getPosition().x());
                        if (zpRow == r && zpCol == c) {
                            pCount++;
                        }
                    }
                }

                projSide.append(pCount == 0 ? "." : Math.min(pCount, 9));
            }

            // Combine all four grids: tiles | plants/structures | zombies | projectiles
            sb.append(tileSide).append("   ||   ").append(leftSide).append("   ||   ").append(rightSide).append("   ||   ").append(projSide).append("\n");
        }

        String legend = "Tiles: _ normal, W water, C shallow coast, G grave, N necromancy, I frozen, L slippery, X crater, B beghouled\n";
        return legend + sb.toString().trim();
    }

    private char tileSymbol(com.ussr.pvz.model.board.terrain.TileType type) {
        return switch (type) {
            case Normal -> '_';
            case Water -> 'W';
            case ShallowCoast -> 'C';
            case Grave -> 'G';
            case Necromancy -> 'N';
            case Frozen -> 'I';
            case Slippery -> 'L';
            case Crater -> 'X';
            case Beghouled -> 'B';
        };
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

        if (cell.getTile() != null) {
            sb.append("type=").append(cell.getTile().getType());
            if (cell.getTile().getSlipperyDirection() != null) {
                sb.append(" (").append(cell.getTile().getSlipperyDirection()).append(")");
            }
            sb.append(" | ");
        }

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
            if (!zombie.isAlive()) continue;

            sb.append(zombie.getAlias()).append(":\n");
            sb.append("position: ").append((int) zombie.getPosition().x())
                    .append(", ").append((int) zombie.getPosition().y()).append("\n");
            sb.append("health: ").append(zombie.getHp()).append("\n");

            sb.append("armor:\n");
            if (zombie.getArmor() != null && !zombie.getArmor().isDestroyed()) {
                sb.append(zombie.getArmor().getArmorType().getName())
                        .append(": ").append(zombie.getArmor().getArmorHp()).append("\n");
            }

            sb.append("effects:\n");
            if (zombie.getStatus() != Zombie.Status.NORMAL) {
                sb.append(statusLabel(zombie.getStatus()));
                if (zombie.getStatusTimeRemaining() > 0) {
                    sb.append(": ").append(formatSeconds(zombie.getStatusTimeRemaining()));
                }
                sb.append("\n");
            }
        }

        return sb.toString().trim();
    }

    private String statusLabel(Zombie.Status status) {
        return switch (status) {
            case FREEZE -> "frozen";
            case BUTTER -> "buttered";
            case FIRED -> "burning";
            case POISONED -> "poisoned";
            case HYPNOTIZED -> "hypnotized";
            default -> status.name().toLowerCase();
        };
    }

    private String formatSeconds(double seconds) {
        return (Math.round(seconds * 10) / 10.0) + "s";
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

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
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
        return clock.getElapsedSeconds();
    }

    public int getTicks() {
        return clock.getTicks();
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
}