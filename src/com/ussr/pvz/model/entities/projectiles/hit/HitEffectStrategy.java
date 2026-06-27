package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.entities.zombies.Zombie;

public interface HitEffectStrategy {
    void apply(Zombie zombie);
    //todo if the projectile move
    // is arc move it should call zombie.takeDamage(int damage , Object moveStrategy)
}
