package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Level;

public interface ChapterEffect {

    default void onStart(GameSession session, Level level) {
    }

    default void onWaveStart(GameSession session, Level level, int waveNumber, boolean isFinalWave) {
    }

    default void onTick(GameSession session, Level level, double deltaTime) {
    }
}