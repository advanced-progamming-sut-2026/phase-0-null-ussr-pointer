package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;

public abstract class Projectile extends GameEntity {
    private int direction; //1 for plant -1 for zombies
    private int damage;

    private MoveStrategy moveStrategy;
    private HitEffectStrategy hitEffectStrategy;
}


