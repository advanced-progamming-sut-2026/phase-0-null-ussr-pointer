package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public interface EffectStatus {
    void effect(Zombie zombie, GameSession session);
}