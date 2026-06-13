package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.entities.zombies.Zombie;

public interface HitEffectStrategy {
    void apply(Zombie zombie);
}
