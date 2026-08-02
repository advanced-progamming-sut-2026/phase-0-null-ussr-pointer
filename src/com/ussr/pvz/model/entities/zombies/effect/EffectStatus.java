package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public interface EffectStatus {
    void effect(
            Zombie zombie,
            GameSession session,
            float delta
    );

    default void onDeath(
            Zombie zombie,
            GameSession session
    ) {
    }
}