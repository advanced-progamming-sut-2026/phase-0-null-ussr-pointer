package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.level.Level;

public class ZombotanyBehavior extends LevelBehavior {

    public ZombotanyBehavior() {
    }

    @Override
    public void onStart(Level level) {
        // Standard setup: sun falling and normal wave scheduling applies.
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
    }

    @Override
    public void onComplete(Level level) {
    }

    @Override
    public boolean isFailed(Level level) {
        // Handled by the standard zombie-reach-house GameSession logic
        return false;
    }
}