package com.ussr.pvz.model.level.ai;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveDirector {
    private final Random random = new Random();

    private final int waveIndex;
    private final Level.Wave waveData;
    private final int initialBudget;
    private int remainingBudget;

    private final List<Zombie> mySpawnedZombies = new ArrayList<>();
    private int totalZombiesSpawned = 0;
    private double spawnTimer = 0.0;
    private final Difficulty difficulty;

    public WaveDirector(int waveIndex, Level.Wave waveData, Difficulty difficulty, double costMultiplier) {
        this.waveIndex = waveIndex;
        this.waveData = waveData;
        this.difficulty = difficulty;
        this.initialBudget = (int) (waveData.cost() * costMultiplier);
        this.remainingBudget = this.initialBudget;
    }

    public void tick(GameSession session, double deltaTime) {
        mySpawnedZombies.removeIf(z -> !z.isAlive());

        if (remainingBudget > 0) {
            int minAffordableCost = getMinZombieCost(session.getLevel());
            if (remainingBudget < minAffordableCost) {
                remainingBudget = 0;
            }
        }

        if (remainingBudget <= 0) return;

        double baseInterval = switch (difficulty) {
            case SIMPLE -> 7.5 * Math.pow(0.85, waveIndex);
            case MEDIUM -> 6.5 * Math.pow(0.80, waveIndex);
            case HARD   -> 5.5 * Math.pow(0.75, waveIndex);
        };
        baseInterval = Math.max(baseInterval, 1.0);

        double waveProgress = initialBudget > 0 ? (double) (initialBudget - remainingBudget) / initialBudget : 0.0;
        double speedMultiplier = switch (difficulty) {
            case SIMPLE -> 0.45;
            case MEDIUM -> 0.60;
            case HARD   -> 0.70;
        };
        double currentInterval = baseInterval * (1.0 - (waveProgress * speedMultiplier));

        spawnTimer += deltaTime;
        if (spawnTimer >= currentInterval) {
            spawnTimer = 0.0;
            executeDifficultyStrategy(session);
        }
    }

    /**
     * Helper method that scans the level's allowed zombie pool to find the absolute lowest point cost.
     * This value dictates the structural floor underneath which no more spawns can physically occur.
     */
    private int getMinZombieCost(Level level) {
        List<Level.AllowedZombie> pool = level.getAllowedZombies();
        if (pool == null || pool.isEmpty()) {
            return Integer.MAX_VALUE; // Safety fallback if no pool configuration is found
        }

        int minCost = Integer.MAX_VALUE;
        for (Level.AllowedZombie allowedZombie : pool) {
            int cost = ZombieFactory.getZombieCost(allowedZombie.id());
            if (cost < minCost) {
                minCost = cost;
            }
        }
        return minCost;
    }

    private void executeDifficultyStrategy(GameSession session) {
        Level level = session.getLevel();
        List<Level.AllowedZombie> pool = level.getAllowedZombies();
        if (pool == null || pool.isEmpty()) return;

        int rows = session.getLawn() != null ? session.getLawn().getRows() : 5;
        int cols = session.getLawn() != null ? session.getLawn().getCols() : 9;

        switch (difficulty) {
            case SIMPLE -> {
                Level.AllowedZombie choice = pool.get(random.nextInt(pool.size()));
                int cost = ZombieFactory.getZombieCost(choice.id());
                if (cost <= remainingBudget) {
                    deploy(session, choice.id(), random.nextInt(rows), cols - 1, cost);
                }
            }
            case MEDIUM -> {
                Level.AllowedZombie choice = pool.get(random.nextInt(pool.size()));
                int cost = ZombieFactory.getZombieCost(choice.id());
                if (cost <= remainingBudget) {
                    int targetedRow = calculateSmartTargetRow(session, rows);
                    deploy(session, choice.id(), targetedRow, cols - 1, cost);
                }
            }
            case HARD -> {
                if (random.nextDouble() < 0.50) {
                    int spawns = 1 + random.nextInt(3);
                    for (int i = 0; i < spawns; i++) {
                        if (remainingBudget <= 0) break;
                        Level.AllowedZombie choice = pool.get(random.nextInt(pool.size()));
                        int cost = ZombieFactory.getZombieCost(choice.id());
                        if (cost <= remainingBudget) {
                            deploy(session, choice.id(), random.nextInt(rows), cols - 1, cost);
                        }
                    }
                } else {
                    int stackSize = 2 + random.nextInt(3);
                    int targetedRow = random.nextInt(rows);
                    for (int i = 0; i < stackSize; i++) {
                        if (remainingBudget <= 0) break;
                        Level.AllowedZombie choice = pool.get(random.nextInt(pool.size()));
                        int cost = ZombieFactory.getZombieCost(choice.id());
                        if (cost <= remainingBudget) {
                            deploy(session, choice.id(), targetedRow, cols - 1, cost);
                        }
                    }
                }
            }
        }
    }

    private int calculateSmartTargetRow(GameSession session, int totalRows) {
        int[] plantCounts = new int[totalRows];
        List<Plant> activePlants = session.getPlants();

        if (activePlants != null) {
            for (Plant p : activePlants) {
                int row = p.getLocation().y();
                if (row >= 0 && row < totalRows) plantCounts[row]++;
            }
        }

        List<Integer> weakestLanes = new ArrayList<>();
        int minPlants = Integer.MAX_VALUE;

        for (int r = 0; r < totalRows; r++) {
            if (plantCounts[r] < minPlants) {
                minPlants = plantCounts[r];
                weakestLanes.clear();
                weakestLanes.add(r);
            } else if (plantCounts[r] == minPlants) {
                weakestLanes.add(r);
            }
        }

        // 85% bias towards weak lanes, 15% random deviation
        if (random.nextDouble() < 0.85 && !weakestLanes.isEmpty()) {
            return weakestLanes.get(random.nextInt(weakestLanes.size()));
        }
        return random.nextInt(totalRows);
    }

    private void deploy(GameSession session, String alias, int row, int col, int cost) {
        Zombie zombie = ZombieFactory.create(alias, row, col);
        session.spawnZombie(zombie);
        mySpawnedZombies.add(zombie);
        totalZombiesSpawned++;
        remainingBudget -= cost;
    }

    public boolean isReadyForNextWave() {
        if (remainingBudget > 0) return false;
        if (totalZombiesSpawned == 0) return true;

        // 80% wave completion calculation rule
        int deadCount = totalZombiesSpawned - mySpawnedZombies.size();
        return deadCount >= (totalZombiesSpawned * 0.80);
    }

    public boolean isFullyCleared() {
        return remainingBudget <= 0 && mySpawnedZombies.isEmpty();
    }

    public int getRemainingBudget() {
        return this.remainingBudget;
    }

    public int getWaveIndex() {
        return this.waveIndex;
    }
}