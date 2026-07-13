package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;

public interface HitEffectStrategy {
    void apply(ArrayList<GameEntity> entities , Projectile projectile);
    int getAreaLength();
}
