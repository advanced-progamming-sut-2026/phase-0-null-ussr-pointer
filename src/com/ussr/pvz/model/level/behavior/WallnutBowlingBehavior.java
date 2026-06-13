package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.entities.plants.BasePlant;
import com.ussr.pvz.model.level.Level;

import java.util.Queue;

public class WallnutBowlingBehavior implements LevelBehavior{
    private final Queue<BasePlant> conveyorBelt;
    private double redLineX;

    public WallnutBowlingBehavior(Queue<BasePlant> conveyorBelt) {
        this.conveyorBelt = conveyorBelt;
    }

    public void roll() {}

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
