package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.level.Level;

import java.util.Queue;

public class WallnutBowlingBehavior implements LevelBehavior {
    private final Queue<Plant> conveyorBelt;
    private double redLineX;

    public WallnutBowlingBehavior(Queue<Plant> conveyorBelt) {
        this.conveyorBelt = conveyorBelt;
    }

    public void roll() {
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
