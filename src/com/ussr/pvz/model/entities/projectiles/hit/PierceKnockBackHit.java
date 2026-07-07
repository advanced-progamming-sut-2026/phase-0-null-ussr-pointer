package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;

public class PierceKnockBackHit implements HitEffectStrategy{
    private final int pierceNumber; // Use -1 for infinite pierce
    private final double knockbackDistance;
    private final ArrayList<Zombie> hitZombies;

    public PierceKnockBackHit(int pierceNumber, double knockbackDistance) {
        this.pierceNumber = pierceNumber;
        this.knockbackDistance = knockbackDistance;
        this.hitZombies = new ArrayList<>();
    }
    @Override
    public void apply(ArrayList<GameEntity> entities, Projectile projectile) {
        if (entities == null || projectile == null) {
            return;
        }

        int damageAmount = projectile.getDamage();

        for (GameEntity target : entities) {
            if (target == null) continue;

            if (pierceNumber != -1 && hitZombies.size() >= pierceNumber) {
                projectile.setAlive(false);
                return;
            }

            if (target instanceof Zombie zombie) {
                if (hitZombies.contains(zombie)) {
                    continue;
                }

                hitZombies.add(zombie);
                zombie.takeDamage(damageAmount);

                // todo : call the zombie.knockback(knockbackdistance) here
                // todo : you can pass the knockback distance to the method too

            } else if (target instanceof InteractableStructure structure) {
                structure.takeDamage(damageAmount);
            }
        }

        if (pierceNumber != -1 && hitZombies.size() >= pierceNumber) {
            projectile.setAlive(false);
        }

    }

    @Override
    public int getAreaLength() {
        return 1;
    }
}
