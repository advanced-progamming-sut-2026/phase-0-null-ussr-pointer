package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.effect.FireEffect;
import com.ussr.pvz.model.entities.zombies.move.ProspectorMove;

public class IceHit implements HitEffectStrategy {
    private int areaLength;

    public IceHit(int areaLength) {
        this.areaLength = areaLength;
    }

    @Override
    public void apply(Zombie zombie) {
        if (zombie == null || !zombie.isAlive()) return;

        if (zombie.getEffectStatus() instanceof FireEffect fireEffect) {
            fireEffect.setLit(false);
        }

        if (zombie.getMoveBehavior() instanceof ProspectorMove prospectorMove) {
            prospectorMove.extinguishDynamite();
        }

        zombie.setStatus(Zombie.Status.FREEZE);

        // TODO: If areaLength > 0, apply splash freeze to surrounding zombies
    }
}