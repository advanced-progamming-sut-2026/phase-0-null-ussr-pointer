package com.ussr.pvz.model.entities.zombies.defense;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class ArmorDefense implements DefenseBehavior {
    @Override
    public int handleDamage(Zombie zombie, int rawDamage, Object damageSource, GameSession session) {
        // Since Zombie.java already has `armor.takeDamage()`, this can act as a passthrough,
        // or you could migrate the Armor deduction logic here in the future!
        return rawDamage;
    }
}