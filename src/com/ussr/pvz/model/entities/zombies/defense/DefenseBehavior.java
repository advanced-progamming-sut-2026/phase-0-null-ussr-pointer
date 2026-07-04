package com.ussr.pvz.model.entities.zombies.defense;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public interface DefenseBehavior {
    /**
     * @return The final damage to be applied. Returns 0 if immune or deflected.
     */
    int handleDamage(Zombie zombie, int rawDamage, Object damageSource, GameSession session);
}