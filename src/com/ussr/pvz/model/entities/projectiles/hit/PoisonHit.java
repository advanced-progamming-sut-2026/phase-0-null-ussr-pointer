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
    private final int tickDamageBonus;

    public PoisonHit(int areaLength) {
        this(areaLength, 0);
    }

    public PoisonHit(int areaLength, int tickDamageBonus) {
        this.areaLength = areaLength;
        this.tickDamageBonus = tickDamageBonus;
    }
    @Override
    public void apply(ArrayList<GameEntity> entities, Projectile projectile) {
        if (entities == null || projectile == null) {
            return;
        }

        projectile.setAlive(false);

        int damageAmount = projectile.getDamage();
        for (GameEntity target : entities) {
            if (target == null || !target.isAlive()) continue;

            switch (target) {
                case Zombie zombie -> {
                    zombie.takeDamage(damageAmount, true);

                    zombie.applyPoison(
                            5 + tickDamageBonus,
                            Zombie.DEFAULT_POISON_DURATION
                    );
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

    public void setAreaLength(int areaLength) {
        this.areaLength = areaLength;
    }
}
