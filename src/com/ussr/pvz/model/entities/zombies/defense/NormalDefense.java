package com.ussr.pvz.model.entities.zombies.defense;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class NormalDefense implements DefenseBehavior {
    @Override
    public int handleDamage(Zombie zombie, int rawDamage, Object damageSource, GameSession session) {
        // Standard zombies just take the full brunt of the damage
        return rawDamage;
    }
}