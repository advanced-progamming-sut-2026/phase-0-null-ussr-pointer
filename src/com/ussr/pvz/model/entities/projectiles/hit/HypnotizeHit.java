package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;

public class HypnotizeHit implements HitEffectStrategy {

    private final int pierceCount;
    private final ArrayList<Zombie> hitZombies = new ArrayList<>();

    public HypnotizeHit(int pierceCount) {
        this.pierceCount = pierceCount;
    }

    @Override
    public void apply(ArrayList<GameEntity> entities, Projectile projectile) {
        if (entities == null || projectile == null) return;

        for (GameEntity entity : entities) {
            if (!(entity instanceof Zombie zombie) || !zombie.isAlive()) continue;
            if (hitZombies.contains(zombie)) continue;
            if (hitZombies.size() >= pierceCount) {
                projectile.setAlive(false);
                return;
            }

            zombie.hypnotize();
            hitZombies.add(zombie);
            projectile.notifyTargetHit(zombie);
        }

        if (hitZombies.size() >= pierceCount) {
            projectile.setAlive(false);
        }
    }

    @Override
    public int getAreaLength() { return 1; }

    @Override
    public boolean canHit(GameEntity target) {
        return !(target instanceof Zombie z) || !hitZombies.contains(z);
    }

    @Override
    public boolean continuesAfterHit() { return true; }
}