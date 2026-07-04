package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.entities.zombies.Zombie;

public class ButterHit implements HitEffectStrategy{
    private int areaLength;

    public ButterHit(int areaLength) {
        this.areaLength = areaLength;
    }
    @Override
    public void apply(Zombie zombie) {

    }
}
