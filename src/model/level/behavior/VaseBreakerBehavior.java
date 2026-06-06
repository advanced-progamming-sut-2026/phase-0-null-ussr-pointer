package model.level.behavior;

import model.board.structures.Vase;
import model.entities.plants.BasePlant;
import model.entities.plants.PlantType;
import model.level.Level;

import java.util.List;

public class VaseBreakerBehavior implements LevelBehavior{
    private final List<Vase> vases;
    private final List<BasePlant> collectedPlants;

    public VaseBreakerBehavior(List<Vase> vases, List<BasePlant> collectedPlants) {
        this.vases = vases;
        this.collectedPlants = collectedPlants;
    }

    public void breakVase(Vase vase) {}
    public void placePlant(BasePlant plant, int row, int column) {}


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
