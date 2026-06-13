package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.level.Level;

public interface LevelBehavior {
    void onStart(Level level);

    void onWaveComplete(Level level, int waveNumber);

    void onComplete(Level level);

    boolean isFailed(Level level);
}
