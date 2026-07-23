package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.plants.Plant;
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

        System.out.println("hittidam");

        projectile.setAlive(false);

        int damageAmount = projectile.getDamage();


        for (GameEntity target : entities) {
            switch (target) {
                case null -> {
                    continue;
                }
                case Zombie zombie -> zombie.takeDamage(damageAmount,projectile);
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
