package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class Projectile extends GameEntity {
    private Vec2 velocity;
    private Vec2 position;
    private int damage;
    private Zombie target;
    private boolean isStunning;

    private MoveStrategy moveStrategy;
    private HitEffectStrategy hitEffectStrategy;

    public Projectile(Vec2 position, Vec2 velocity , Zombie zombie, int damage, MoveStrategy moveStrategy, HitEffectStrategy hitEffectStrategy) {
        this.position = position;
        this.velocity = velocity;
        this.target = zombie;
        this.damage = damage;
        this.moveStrategy = moveStrategy;
        this.hitEffectStrategy = hitEffectStrategy;
        this.isStunning = false;
    }

    public void setStunning(boolean isStunning) { this.isStunning = isStunning; }


    @Override
    public void tick() {

    }
}


