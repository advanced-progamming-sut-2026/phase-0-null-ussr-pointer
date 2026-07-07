package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.level.Level;

public class TimedWarBehavior implements LevelBehavior {

    public enum LimitationType { ZOMBIE, SUN }

    private final LimitationType limitationType;
    private final int targetValue;
    private int counter = 0;

    public TimedWarBehavior(LimitationType limitationType, int targetValue) {
        this.limitationType = limitationType;
        this.targetValue = targetValue;
    }

    @Override
    public void onStart(Level level) {
        // Initialization if needed
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {}

    @Override
    public void onComplete(Level level) {}

    @Override
    public boolean isFailed(Level level) {
        if (App.getGameSession().getElapsedSeconds() > level.getTimeLimitSeconds()) {
            return counter < targetValue;
        }
        return false;
    }

    public void triggerSunCollected(int amount) {
        if (limitationType == LimitationType.SUN) counter += amount;
    }

    public void triggerZombieDied() {
        if (limitationType == LimitationType.ZOMBIE) counter++;
    }
}