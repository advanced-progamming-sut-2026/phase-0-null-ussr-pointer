package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy;
import com.ussr.pvz.model.entities.projectiles.hit.PierceKnockBackHit;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.util.Vec2;

public class KnockBackBlast implements PlantFoodEffect {
    private final double knockbackDistance;

    public KnockBackBlast(double knockbackDistance) {
        this.knockbackDistance = knockbackDistance;
    }

    public KnockBackBlast() {
        this(3.0);
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (user == null || session == null || user.getPosition() == null) return;

        Vec2 userPos = user.getPosition();
        Vec2 vel = new Vec2(4, 0);

        MoveStrategy moveStrategy = new StraightMove();
        HitEffectStrategy hitEffectStrategy = new PierceKnockBackHit(Integer.MAX_VALUE, knockbackDistance);

        Projectile projectile = new Projectile(userPos, vel, null, user.getDamage(), moveStrategy, hitEffectStrategy);
        session.addProjectile(projectile);
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        // Instant trigger; no permanent stat modifiers
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        // Instant trigger; no continuous duration logic needed
    }
}