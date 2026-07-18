package com.ussr.pvz.model.entities.projectiles.hit;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import java.util.ArrayList;

public interface HitEffectStrategy {
    // todo: handle area length in all classes right now it is useless
    void apply(ArrayList<GameEntity> entities , Projectile projectile);
    int getAreaLength();
}
