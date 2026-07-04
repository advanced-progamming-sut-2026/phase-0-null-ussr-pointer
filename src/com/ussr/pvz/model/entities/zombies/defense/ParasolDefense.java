package com.ussr.pvz.model.entities.zombies.defense;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class ParasolDefense implements DefenseBehavior {
    @Override
    public int handleDamage(Zombie zombie, int rawDamage, Object damageSource, GameSession session) {
        if (damageSource instanceof Projectile projectile) {
            // Deflects lobbed (ArcMove) projectiles completely
            if (projectile.getMoveStrategy() instanceof ArcMove) {
                // Optional: You could publish a GameEvent here for a "Deflect" animation/sound!
                return 0;
            }
        }
        return rawDamage;
    }
}