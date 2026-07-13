package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Level;

public interface ChapterEffect {
    //todo : make a class for ancient Egypt and handle the sand storms there instead of handling them into behaviors
    void onWaveStart(GameSession session, Level level, int waveNumber, boolean isFinalWave);
}