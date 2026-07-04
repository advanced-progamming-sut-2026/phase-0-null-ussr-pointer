package com.ussr.pvz.model.entities.zombies.defense;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.FireHit;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class ImmuneDefense implements DefenseBehavior {
    @Override
    public int handleDamage(Zombie zombie, int rawDamage, Object damageSource, GameSession session) {
        if (damageSource instanceof Projectile projectile) {
            // If the projectile applies fire, the Dragon Imp takes 0 damage
            if (projectile.getHitEffectStrategy() instanceof FireHit) {
                return 0;
            }
        }
        return rawDamage;
    }
}