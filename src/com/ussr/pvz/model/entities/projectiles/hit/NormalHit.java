package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import java.util.ArrayList;

public class NormalHit implements HitEffectStrategy {
    private int areaLength;

    public NormalHit(int areaLength) {
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
            if (target == null) continue;

            if (target instanceof Zombie zombie) {
                zombie.takeDamage(damageAmount);
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
