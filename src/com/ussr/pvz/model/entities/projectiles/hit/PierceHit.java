package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.BounceMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import java.util.ArrayList;

public class PierceHit implements HitEffectStrategy {
    private final int pierceNumber;
    private final ArrayList<Zombie> hitZombies;

    public PierceHit(int pierceNumber) {
        this.pierceNumber = pierceNumber;
        hitZombies = new ArrayList<>();
        System.out.println(this.pierceNumber);
    }

    @Override
    public void apply(ArrayList<GameEntity> entities, Projectile projectile) {
        if (entities == null || projectile == null) return;

        int damageAmount = projectile.getDamage();
        boolean shouldDestroyProjectile = false;

        for (GameEntity target : entities) {
            if (target == null || !target.isAlive()) continue;

            if (processTarget(target, damageAmount, projectile)) {
                shouldDestroyProjectile = true;
            }
        }

        if (shouldDestroyProjectile) {
            projectile.setAlive(false);
        }
        if(projectile.getMoveStrategy() instanceof BounceMove bounceMove) {
            bounceMove.bounce(projectile);
        }
    }

    private boolean processTarget(GameEntity target, int damageAmount, Projectile projectile) {
        if (target instanceof Zombie zombie) {
            if (!hitZombies.contains(zombie)) {
                if (hitZombies.size() < pierceNumber) {
                    zombie.takeDamage(damageAmount, projectile);
                    hitZombies.add(zombie);
                    System.out.println("hittidam");
                }
                return hitZombies.size() >= pierceNumber;
            }
            return false;
        } else if (target instanceof Plant plant) {
            plant.takeDamage(damageAmount);
            return true;
        } else if (target instanceof InteractableStructure structure) {
            structure.takeDamage(damageAmount);
            return true;
        }
        return false;
    }

    @Override
    public int getAreaLength() {
        return 1;
    }
}