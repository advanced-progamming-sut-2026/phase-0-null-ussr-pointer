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
                // Pass the projectile as the damage source
                zombie.takeDamage(damageAmount, projectile);
                zombie.setStatus(Zombie.Status.BUTTER);

                // Wrap the current move behavior to stun them for a duration (e.g., 4 seconds)
                if (!(zombie.getMoveBehavior() instanceof com.ussr.pvz.model.entities.zombies.move.StunnedMoveBehavior)) {
                    zombie.setMoveBehavior(new com.ussr.pvz.model.entities.zombies.move.StunnedMoveBehavior(zombie.getMoveBehavior(), 4.0));
                }

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
