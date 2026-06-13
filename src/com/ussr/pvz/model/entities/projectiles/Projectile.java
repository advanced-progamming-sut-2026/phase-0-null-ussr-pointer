package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;

public abstract class Projectile extends GameEntity {
    private final int direction; //1 for plant -1 for zombies
    private final int damage;

    private final MoveStrategy moveStrategy;
    private HitEffectStrategy hitEffectStrategy;
}


