package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.level.Level;
import java.util.HashMap;
import java.util.Map;

public class BounceBehavior extends LevelBehavior {
    private int currentScore = 0;

    private double lastKillTime = 0;
    private int simultaneousKillCount = 0;
    private final Map<String, Double> zombieSpawnTimes = new HashMap<>();

    public BounceBehavior() {
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

    private void onZombieSpawned(GameEvent.ZombieSpawned event) {
        zombieSpawnTimes.put(event.alias(), App.getGameSession().getElapsedSeconds());
    }

    private void onZombieDied(GameEvent.ZombieDied event) {
        GameSession session = App.getGameSession();
        if (session == null) return;

        double currentTime = session.getElapsedSeconds();
        int pointsEarned = 10;

        if (currentTime == lastKillTime) {
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

        if ("LawnMower".equals(event.killerPlantName())) {
            pointsEarned = 0;
        }

        if (event.alias().toLowerCase().contains("gargantuar")) {
            pointsEarned += 500;
        }

        currentScore += pointsEarned;
        lastKillTime = currentTime;
        zombieSpawnTimes.remove(event.alias());
    }

    @Override
    public void onComplete(Level level) {
        super.onComplete(level);
        if (App.getAccount() != null) {
            int previousHigh = App.getAccount().getScoreRecord().getScore();
            if (currentScore > previousHigh) {
                App.getAccount().getScoreRecord().setScore(currentScore);
            }
        }
    }

    @Override
    public boolean isFailed(Level level) {
        return false;
    }
}