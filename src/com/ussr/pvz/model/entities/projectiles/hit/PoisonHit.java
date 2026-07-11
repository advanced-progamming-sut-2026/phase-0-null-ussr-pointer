package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;

public class PoisonHit implements HitEffectStrategy {
    private int areaLength;

    public PoisonHit(int areaLength) {
        this.areaLength = areaLength;
    }
    @Override
    public void apply(ArrayList<GameEntity> entities, Projectile projectile) {
        if (entities == null || projectile == null) {
            return;
        }

        projectile.setAlive(false);

        int damageAmount = projectile.getDamage();
        long projectileLane = Math.round(projectile.getPosition().y());

        for (GameEntity target : entities) {
            if (target == null || !target.isAlive()) continue;

            if (target instanceof Zombie zombie) {
                zombie.takeDamage(damageAmount , true);

                zombie.setStatus(Zombie.Status.POISONED);

            } else if (target instanceof Plant plant) {
                plant.takeDamage(damageAmount);

            } else if (target instanceof InteractableStructure structure) {
                structure.takeDamage(damageAmount);
            }
        }
    }

    @Override
    public int getAreaLength() {
        return 1;
    }
}
