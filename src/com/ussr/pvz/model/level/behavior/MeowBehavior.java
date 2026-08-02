package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MeowBehavior extends LevelBehavior {
    private int currentScore = 0;

    private double lastKillTime = -1.0;
    private int simultaneousKillCount = 0;
    private final Map<String, Double> zombieSpawnTimes = new HashMap<>();

    private final Random random = new Random();
    private double spawnTimer = 0.0;

    private static final double BASE_SPAWN_INTERVAL = 8.0;
    private static final double MIN_SPAWN_INTERVAL = 1.5;
    private static final int SCORE_PER_INTERVAL_STEP = 400;
    private static final double INTERVAL_STEP_AMOUNT = 0.3;

    private static final int SCORE_FOR_BURST_2 = 1500;
    private static final int SCORE_FOR_BURST_3 = 4000;
    private static final int SCORE_FOR_BURST_4 = 8000;

    private static final List<String> TOUGH_ALIASES = List.of("Gargantuar", "Armor2", "Armor4");

    public MeowBehavior() {
        this.autoWinOnWavesClear = false;
    }

    @Override
    public void onStart(Level level) {
        super.onStart(level);
        GameSession session = App.getGameSession();
        if (session != null) {
            session.getEventBus().subscribe(GameEvent.ZombieSpawned.class, this::onZombieSpawned);
            session.getEventBus().subscribe(GameEvent.ZombieDied.class, this::onZombieDied);
        }
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);
        if (session.isGameOver()) {
            syncScoreToProfile();
            return;
        }

        spawnTimer += deltaTime;
        double interval = currentSpawnInterval();
        if (spawnTimer >= interval) {
            spawnTimer = 0.0;
            int burst = currentBurstSize();
            for (int i = 0; i < burst; i++) {
                spawnRandomZombie(session);
            }
        }
    }

    private double currentSpawnInterval() {
        int steps = currentScore / SCORE_PER_INTERVAL_STEP;
        double interval = BASE_SPAWN_INTERVAL - (steps * INTERVAL_STEP_AMOUNT);
        return Math.max(MIN_SPAWN_INTERVAL, interval);
    }

    private int currentBurstSize() {
        if (currentScore >= SCORE_FOR_BURST_4) return 4;
        if (currentScore >= SCORE_FOR_BURST_3) return 3;
        if (currentScore >= SCORE_FOR_BURST_2) return 2;
        return 1;
    }

    private void spawnRandomZombie(GameSession session) {
        Level level = session.getLevel();
        if (level == null) return;
        List<Level.AllowedZombie> pool = level.getAllowedZombies();
        if (pool == null || pool.isEmpty()) return;

        Level.AllowedZombie chosen = pickWeighted(pool);
        if (chosen == null) return;

        int rows = session.getLawn() != null ? session.getLawn().getRows() : 5;
        int cols = session.getLawn() != null ? session.getLawn().getCols() : 9;
        int row = random.nextInt(rows);

        Zombie zombie = ZombieFactory.create(chosen.id(), row, cols);
        session.spawnZombie(zombie);
    }

    private Level.AllowedZombie pickWeighted(List<Level.AllowedZombie> pool) {
        double toughBoost = 1.0 + (currentScore / 2000.0);

        int totalWeight = 0;
        int[] effectiveWeights = new int[pool.size()];
        for (int i = 0; i < pool.size(); i++) {
            Level.AllowedZombie z = pool.get(i);
            int base = Math.max(z.weight(), 0);
            boolean isTough = TOUGH_ALIASES.stream().anyMatch(z.id()::contains);
            int weight = isTough ? (int) Math.round(base * toughBoost) : base;
            effectiveWeights[i] = weight;
            totalWeight += weight;
        }

        if (totalWeight <= 0) {
            return pool.get(random.nextInt(pool.size()));
        }

        int roll = random.nextInt(totalWeight);
        for (int i = 0; i < pool.size(); i++) {
            roll -= effectiveWeights[i];
            if (roll < 0) return pool.get(i);
        }
        return pool.get(pool.size() - 1);
    }

    private void onZombieSpawned(GameEvent.ZombieSpawned event) {
        GameSession session = App.getGameSession();
        if (session != null) {
            zombieSpawnTimes.put(event.alias(), session.getElapsedSeconds());
        }
    }

    private void onZombieDied(GameEvent.ZombieDied event) {
        GameSession session = App.getGameSession();
        if (session == null) return;

        if ("LawnMower".equalsIgnoreCase(event.killerPlantName())) {
            return;
        }

        double currentTime = session.getElapsedSeconds();
        int pointsEarned = 10;

        if (Math.abs(currentTime - lastKillTime) < 0.05) {
            simultaneousKillCount++;
            pointsEarned += (15 * simultaneousKillCount);
        } else {
            simultaneousKillCount = 0;
        }

        Double spawnTime = zombieSpawnTimes.get(event.alias());
        if (spawnTime != null && (currentTime - spawnTime) <= 5.0) {
            pointsEarned += 20;
        }

        if (event.x() >= 7.0) {
            pointsEarned += 25;
        }

        if (event.alias().toLowerCase().contains("gargantuar") || event.alias().toLowerCase().contains("boss")) {
            pointsEarned += 500;
        }

        currentScore += pointsEarned;
        lastKillTime = currentTime;
        zombieSpawnTimes.remove(event.alias());

        syncScoreToProfile();
    }

    @Override
    public void onComplete(Level level) {
        super.onComplete(level);
        syncScoreToProfile();
    }

    private void syncScoreToProfile() {
        if (App.getAccount() != null && App.getAccount().getScoreRecord() != null) {
            int previousHigh = App.getAccount().getScoreRecord().getScore();
            if (currentScore > previousHigh) {
                App.getAccount().getScoreRecord().setScore(currentScore);
            }
        }
    }

    public int getCurrentScore() {
        return currentScore;
    }

    @Override
    public boolean isFailed(Level level) {
        return false;
    }
}