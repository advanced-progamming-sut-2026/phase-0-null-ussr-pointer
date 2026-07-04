package com.ussr.pvz.model.entities.zombies.defense;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class JesterDefense implements DefenseBehavior {
    @Override
    public int handleDamage(Zombie zombie, int rawDamage, Object damageSource, GameSession session) {
        if (damageSource instanceof Projectile projectile) {
            // Jester juggles standard straight shots and lobbed shots
            if (projectile.getMoveStrategy() instanceof StraightMove ||
                    projectile.getMoveStrategy() instanceof ArcMove) {

                // TODO: Spawn a mirrored projectile aimed back at the plants!

                return 0; // Takes no damage from juggled objects
            }
        }
        return rawDamage;
    }
}