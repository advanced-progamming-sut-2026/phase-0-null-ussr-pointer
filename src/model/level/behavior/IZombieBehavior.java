package model.level.behavior;

import model.entities.zombies.Zombie;
import model.level.Level;

import java.util.Map;

public class IZombieBehavior implements LevelBehavior{
    private final int zombiesPerRow = 5;

    public void placeZombie(Zombie zombie, double x, double y) {}

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
