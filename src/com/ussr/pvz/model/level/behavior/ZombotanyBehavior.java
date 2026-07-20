package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Level;

public class ZombotanyBehavior extends LevelBehavior {

    public ZombotanyBehavior() {
    }

    @Override
    public void onStart(Level level) {
        super.onStart(level);
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
        super.onWaveComplete(level, waveNumber);
    }

    @Override
    public void onComplete(Level level) {
        super.onComplete(level);
    }

    @Override
    public boolean isFailed(Level level) {
        return false;
    }
}