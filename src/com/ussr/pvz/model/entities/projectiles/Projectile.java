package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;
import com.ussr.pvz.model.util.Vec2;

public class Projectile extends GameEntity {
    private Vec2 direction;
    private Vec2 position;
    private int damage;

    private MoveStrategy moveStrategy;
    private HitEffectStrategy hitEffectStrategy;

    public Projectile(Vec2 position, Vec2 direction, int damage, MoveStrategy moveStrategy, HitEffectStrategy hitEffectStrategy) {
        this.position = position;
        this.direction = direction;
        this.damage = damage;
        this.moveStrategy = moveStrategy;
        this.hitEffectStrategy = hitEffectStrategy;
    }


    @Override
    public void tick() {

    }
}


