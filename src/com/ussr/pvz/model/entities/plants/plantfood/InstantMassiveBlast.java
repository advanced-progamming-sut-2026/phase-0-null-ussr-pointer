package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.*;
import com.ussr.pvz.model.entities.projectiles.move.BounceMove;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.util.Vec2;

public class InstantMassiveBlast implements PlantFoodEffect{
    private final double baseDamageMultiplier;
    private final boolean scalingByStackSize;

    public InstantMassiveBlast(double baseDamageMultiplier, boolean scalingByStackSize) {
        this.baseDamageMultiplier = baseDamageMultiplier;
        this.scalingByStackSize = scalingByStackSize;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (user.getShootingVectors() == null || user.getShootingVectors().isEmpty()) return;

        int firingHeadsCount = 1;
        if (this.scalingByStackSize) {
            firingHeadsCount = user.getStackNumber();
        }

        Vec2 direction = user.getShootingVectors().get(0);
        Vec2 velocity = direction.normalize().scale(30.0);

        int totalDamage = (int) (user.getDamage() * this.baseDamageMultiplier);
        for (int i = 0; i < firingHeadsCount; i++) {
            HitEffectStrategy hitEffectStrategy = handleHitEffect(user);
            MoveStrategy moveStrategy = handleMoveStrategy(user);
            session.getProjectiles().add(new Projectile(
                    new Vec2(user.getPosition().x() + (i * 10), user.getPosition().y()),
                    velocity,
                    null,
                    totalDamage,
                    moveStrategy,
                    hitEffectStrategy
            ));
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {

    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {

    }

    private HitEffectStrategy handleHitEffect(Plant user) {
        if(user.getTags().contains(Tag.FIRE))
            return new FireHit(1);
        if(user.getTags().contains(Tag.ICE))
            return new IceHit(1);
        if(user.getTags().contains(Tag.PIERCE))
            return new PierceHit(-1);
        if(user.getTags().contains(Tag.POISON))
            return new PoisonHit(1);
        return new NormalHit(1);
    }

    private MoveStrategy handleMoveStrategy(Plant user) {
        if(user.getTags().contains(Tag.BOUNCE))
            return new BounceMove(9.8 , 1);
        return new StraightMove();
    }
}
