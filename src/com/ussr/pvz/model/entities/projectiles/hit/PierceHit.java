package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.entities.zombies.Zombie;

public class PierceHit implements HitEffectStrategy {
    private int pierceNumber;
    public PierceHit(int pierceNumber) {
        this.pierceNumber = pierceNumber;
    }
    @Override
    public void apply(Zombie zombie) {

    }
}
