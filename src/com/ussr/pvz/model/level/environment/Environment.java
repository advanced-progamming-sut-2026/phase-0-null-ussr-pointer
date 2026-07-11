package com.ussr.pvz.model.level.environment;

import com.ussr.pvz.model.engine.GameSession;

public interface Environment {
    void onStart(GameSession session);
    void tick(GameSession session, double deltaTime);
}