package com.ussr.pvz.model.entities.zombies.attack;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public interface AttackBehavior {
    void attack(
            Zombie zombie,
            GameSession session,
            float delta
    );
}
