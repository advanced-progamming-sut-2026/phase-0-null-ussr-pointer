package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.entities.plants.BasePlant;
import com.ussr.pvz.model.level.Level;

import java.util.List;

public class ZombotanyBehavior implements LevelBehavior{
    private final List<BasePlant> availableHybrids;

    public ZombotanyBehavior(List<BasePlant> availableHybrids) {
        this.availableHybrids = availableHybrids;
    }

    @Override
    public void onStart(Level level) {

    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {

    }

    @Override
    public void onComplete(Level level) {

    }

    @Override
    public boolean isFailed(Level level) {
        return false;
    }
}
