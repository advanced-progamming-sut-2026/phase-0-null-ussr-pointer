package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.entities.items.SeedPackDrop;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.level.Level;

import java.util.List;

public class VaseBreakerBehavior implements LevelBehavior {
    private final List<Vase> vases;
    //todo use that somewhere
    private final List<Plant> collectedPlants;

    public VaseBreakerBehavior(List<Vase> vases, List<Plant> collectedPlants) {
        this.vases = vases;
        this.collectedPlants = collectedPlants;
    }

    public void breakVase(Vase vase) {
        vase.onDestroy(App.getGameSession());
        vases.remove(vase);
    }

    public void placePlant(SeedPackDrop seedPackDrop, int row, int column) {
        //todo this calls by user
        seedPackDrop.applyReward(App.getGameSession(),App.getAccount(),row , column);
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
