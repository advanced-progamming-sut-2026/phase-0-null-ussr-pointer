package model.level.behavior;

import model.level.Level;

public interface LevelBehavior {
    void onStart(Level level);

    void onWaveComplete(Level level, int waveNumber);

    void onComplete(Level level);

    boolean isFailed(Level level);
}
