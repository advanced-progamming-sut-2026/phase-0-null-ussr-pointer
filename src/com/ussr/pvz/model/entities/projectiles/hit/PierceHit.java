package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import java.util.ArrayList;

public class PierceHit implements HitEffectStrategy {
    private int pierceNumber;
    private ArrayList<Zombie> hitZombies;
    public PierceHit(int pierceNumber) {
        this.pierceNumber = pierceNumber;
        hitZombies = new ArrayList<>();
    }
    @Override
    public void apply(ArrayList<GameEntity> entities, Projectile projectile) {
        if (entities == null || projectile == null) {
            return;
        }

        int damageAmount = projectile.getDamage();
        long projectileLane = Math.round(projectile.getPosition().y());
        boolean shouldDestroyProjectile = false;

        for (GameEntity target : entities) {
            if (target == null || !target.isAlive()) continue;

            switch (target) {
                case Zombie zombie -> {
                    if (!hitZombies.contains(zombie)) {
                        if (hitZombies.size() < pierceNumber) {
                            zombie.takeDamage(damageAmount,projectile);
                            hitZombies.add(zombie);
                        }

                        if (hitZombies.size() >= pierceNumber) {
                            shouldDestroyProjectile = true;
                        }
                    }
                }
                case Plant plant -> {
                    plant.takeDamage(damageAmount);
                    shouldDestroyProjectile = true;
                }
                case InteractableStructure structure -> {
                    structure.takeDamage(damageAmount);

                    shouldDestroyProjectile = true;
                }
                default -> {
                }
            }
        }

        if (shouldDestroyProjectile) {
            projectile.setAlive(false);
        }
    }

    @Override
    public int getAreaLength() {
        return 1;
    }
}
