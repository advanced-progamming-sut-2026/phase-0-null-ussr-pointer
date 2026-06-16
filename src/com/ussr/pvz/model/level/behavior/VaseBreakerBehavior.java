package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.level.Level;

import java.util.List;

public class VaseBreakerBehavior implements LevelBehavior {
    private final List<Vase> vases;
    private final List<Plant> collectedPlants;

    public VaseBreakerBehavior(List<Vase> vases, List<Plant> collectedPlants) {
        this.vases = vases;
        this.collectedPlants = collectedPlants;
    }

    public void breakVase(Vase vase) {
    }

    public void placePlant(Plant plant, int row, int column) {
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
