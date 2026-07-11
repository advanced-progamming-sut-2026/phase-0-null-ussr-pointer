package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Level;

public interface ChapterEffect {
    void onWaveStart(GameSession session, Level level, int waveNumber, boolean isFinalWave);
}