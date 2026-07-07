package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;

public class ButterHit implements HitEffectStrategy{
    private int areaLength;

    public ButterHit(int areaLength) {
        this.areaLength = areaLength;
    }
    @Override
    public void apply(ArrayList<GameEntity> entities, Projectile projectile) {
        if (entities == null || projectile == null) {
            return;
        }

        projectile.setAlive(false);

        int damageAmount = projectile.getDamage();

        for (GameEntity target : entities) {
            if (target == null) continue;

            if (target instanceof Zombie zombie) {
                zombie.takeDamage(damageAmount);

                zombie.setStatus(Zombie.Status.BUTTER);

                // todo: Call your zombie immobilization/stun method here

            } else if (target instanceof InteractableStructure structure) {
                structure.takeDamage(damageAmount);
            }
        }
    }

    @Override
    public int getAreaLength() {
        return areaLength;
    }
}
