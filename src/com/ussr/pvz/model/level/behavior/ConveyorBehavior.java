package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.level.Level;

public class ConveyorBehavior implements LevelBehavior {
    @Override
    public void onStart(Level level) {
        // Disable sun falling since conveyor plants do not require sun
        level.setSunFalling(false);
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
        // Specific wave logic for conveyor levels (if any)
    }

    @Override
    public void onComplete(Level level) {
        // Cleanup or reward distribution logic
    }

    @Override
    public boolean isFailed(Level level) {
        // Relies on standard game over conditions managed externally
        return false;
    }
}