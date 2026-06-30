package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.effect.FireEffect;

public class FireHit implements HitEffectStrategy {
    private int areaLength;

    public FireHit(int areaLength) {
        this.areaLength = areaLength;
    }

    @Override
    public void apply(Zombie zombie) {
        if (zombie == null || !zombie.isAlive()) return;
        
        if (zombie.getEffectStatus() instanceof FireEffect fireEffect) {
            fireEffect.setLit(true);
        }

        zombie.setStatus(Zombie.Status.FIRED);

        // TODO: If areaLength > 0, apply splash fire to surrounding zombies
    }
}