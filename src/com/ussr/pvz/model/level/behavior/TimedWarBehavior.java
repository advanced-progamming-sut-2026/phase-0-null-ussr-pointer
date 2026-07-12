package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;

public class TimedWarBehavior extends LevelBehavior {

    public enum LimitationType { ZOMBIE, SUN }

    private final LimitationType limitationType;
    private final int targetValue;
    private int counter = 0;

    public TimedWarBehavior(LimitationType limitationType, int targetValue) {
        this.limitationType = limitationType;
        this.targetValue = targetValue;
        this.autoWinOnWavesClear = false;
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);

        if (levelCompleted || session.isGameOver()) return;

        Level level = session.getLevel();
        if (level == null) return;

        // Evaluate objectives once the time limit expires
        if (session.getElapsedSeconds() > level.getTimeLimitSeconds()) {
            if (counter >= targetValue) {
                onComplete(level);
            } else {
                session.getEventBus().publish(new GameEvent.GameOver());
            }
        }
    }

    @Override
    public void onZombieDied(GameSession session, Zombie zombie) {
        if (limitationType == LimitationType.ZOMBIE) {
            counter++;
        }
    }

    @Override
    public void onSunCollected(GameSession session, int amount) {
        if (limitationType == LimitationType.SUN) {
            counter += amount;
        }
    }

    public int getCounter() {
        return counter;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public LimitationType getLimitationType() {
        return limitationType;
    }
}