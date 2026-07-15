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

    // PRECALCULATED CONSTANTS
    private final double baseInterval;
    private final double speedMultiplier;
    private final SpawnStrategy spawnStrategy;

    // A lightweight functional interface to store our spawning strategies
    @FunctionalInterface
    private interface SpawnStrategy {
        void execute(GameSession session, List<Level.AllowedZombie> pool, int rows, int cols);
    }

    public WaveDirector(int waveIndex, Level.Wave waveData, Difficulty difficulty, double costMultiplier) {
        this.waveIndex = waveIndex;
        this.waveData = waveData;
        this.difficulty = difficulty;
        this.initialBudget = (int) (waveData.cost() * costMultiplier);
        this.remainingBudget = this.initialBudget;

        // 1. Precalculate the base spawn interval once
        double calculatedBase = switch (difficulty) {
            case SIMPLE -> 7.5 * Math.pow(0.85, waveIndex);
            case MEDIUM -> 6.5 * Math.pow(0.80, waveIndex);
            case HARD   -> 5.5 * Math.pow(0.75, waveIndex);
        };
        this.baseInterval = Math.max(calculatedBase, 1.0);

        // 2. Precalculate the speed multiplier once
        this.speedMultiplier = switch (difficulty) {
            case SIMPLE -> 0.45;
            case MEDIUM -> 0.60;
            case HARD   -> 0.70;
        };

        // 3. Select the correct spawning strategy method once on startup
        this.spawnStrategy = switch (difficulty) {
            case SIMPLE -> this::executeSimpleStrategy;
            case MEDIUM -> this::executeMediumStrategy;
            case HARD   -> this::executeHardStrategy;
        };
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

        // Uses precalculated baseInterval and speedMultiplier directly without switches
        double waveProgress = initialBudget > 0 ? (double) (initialBudget - remainingBudget) / initialBudget : 0.0;
        double currentInterval = baseInterval * (1.0 - (waveProgress * speedMultiplier));

        spawnTimer += deltaTime;
        if (spawnTimer >= currentInterval) {
            spawnTimer = 0.0;

            // Fetch level-specific environment values once before invoking the strategy
            Level level = session.getLevel();
            List<Level.AllowedZombie> pool = level.getAllowedZombies();
            if (pool == null || pool.isEmpty()) return;

            int rows = session.getLawn() != null ? session.getLawn().getRows() : 5;
            int cols = session.getLawn() != null ? session.getLawn().getCols() : 9;

            // Polymorphically execute strategy without switch blocks!
            spawnStrategy.execute(session, pool, rows, cols);
        }
    }

    private int getMinZombieCost(Level level) {
        List<Level.AllowedZombie> pool = level.getAllowedZombies();
        if (pool == null || pool.isEmpty()) {
            return Integer.MAX_VALUE;
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

    private Level.AllowedZombie pickWeighted(List<Level.AllowedZombie> pool) {
        int totalWeight = 0;
        for (Level.AllowedZombie z : pool) {
            totalWeight += Math.max(z.weight(), 0);
        }

        if (totalWeight <= 0) {
            return pool.get(random.nextInt(pool.size()));
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (Level.AllowedZombie z : pool) {
            cumulative += Math.max(z.weight(), 0);
            if (roll < cumulative) {
                return z;
            }
        }

        return pool.getLast();
    }

    private void executeSimpleStrategy(GameSession session, List<Level.AllowedZombie> pool, int rows, int cols) {
        Level.AllowedZombie choice = pickWeighted(pool);
        int cost = ZombieFactory.getZombieCost(choice.id());
        if (cost <= remainingBudget) {
            deploy(session, choice.id(), random.nextInt(rows), cols - 1, cost);
        }
    }

    private void executeMediumStrategy(GameSession session, List<Level.AllowedZombie> pool, int rows, int cols) {
        Level.AllowedZombie choice = pickWeighted(pool);
        int cost = ZombieFactory.getZombieCost(choice.id());
        if (cost <= remainingBudget) {
            int targetedRow = calculateSmartTargetRow(session, rows);
            deploy(session, choice.id(), targetedRow, cols - 1, cost);
        }
    }

    private void executeHardStrategy(GameSession session, List<Level.AllowedZombie> pool, int rows, int cols) {
        if (random.nextDouble() < 0.50) {
            int spawns = 1 + random.nextInt(3);
            for (int i = 0; i < spawns; i++) {
                if (remainingBudget <= 0) break;
                executeSimpleStrategy(session, pool, rows, cols);
            }
        } else {
            int stackSize = 2 + random.nextInt(3);
            int targetedRow = random.nextInt(rows);
            for (int i = 0; i < stackSize; i++) {
                if (remainingBudget <= 0) break;
                Level.AllowedZombie choice = pickWeighted(pool);
                int cost = ZombieFactory.getZombieCost(choice.id());
                if (cost <= remainingBudget) {
                    deploy(session, choice.id(), targetedRow, cols - 1, cost);
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