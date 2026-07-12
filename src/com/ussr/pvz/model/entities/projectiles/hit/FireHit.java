package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.effect.FireEffect;
import com.ussr.pvz.model.entities.zombies.move.ProspectorMove;

import java.util.ArrayList;

public class FireHit implements HitEffectStrategy {
    private int areaLength;

    public FireHit(int areaLength) {
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

            switch (target) {
                case Zombie zombie -> {
                    zombie.takeDamage(damageAmount);

                    if (zombie.getEffectStatus() instanceof FireEffect fireEffect) {
                        fireEffect.setLit(true);
                    }

                    if (zombie.getMoveBehavior() instanceof ProspectorMove prospectorMove) {
                        prospectorMove.litDynamite();
                    }
                    zombie.setStatus(Zombie.Status.FIRED);
                }
                case Plant plant -> plant.takeDamage(damageAmount);
                case InteractableStructure structure -> structure.takeDamage(damageAmount);
                default -> {
                }
            }
        }
    }

    @Override
    public int getAreaLength() {
        return areaLength;
    }
}