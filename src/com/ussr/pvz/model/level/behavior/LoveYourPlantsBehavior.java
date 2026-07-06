package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.level.Level;

public class LoveYourPlantsBehavior implements LevelBehavior {
    private int counter = 0;
    private int value = 5;

    @Override
    public void onStart(Level level) {
        //nada
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {

    }

    @Override
    public void onComplete(Level level) {

    }

    @Override
    public boolean isFailed(Level level) {
        return counter > value;
    }

    public void triggerPlantDied() {
        counter++;
    }
}
