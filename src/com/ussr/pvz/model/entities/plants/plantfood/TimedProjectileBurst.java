package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.*;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.List;

public class TimedProjectileBurst implements PlantFoodEffect {
    private final double duration;
    private final double fireRate;
    private final int giantPeaCount;
    private final double giantPeaMultiplier;
    private final boolean freezeLaneOnTrigger;
    private final Tag resetLifespanTag;
    private final List<Vec2> temporaryVectors;

    public TimedProjectileBurst(double duration, double fireRate, int giantPeaCount,
                                double giantPeaMultiplier, boolean freezeLaneOnTrigger,
                                Tag resetLifespanTag, List<Vec2> temporaryVectors) {
        this.duration = duration;
        this.fireRate = fireRate;
        this.giantPeaCount = giantPeaCount;
        this.giantPeaMultiplier = giantPeaMultiplier;
        this.freezeLaneOnTrigger = freezeLaneOnTrigger;
        this.resetLifespanTag = resetLifespanTag;
        this.temporaryVectors = temporaryVectors;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        user.setPlantFoodTimer(this.duration);
        user.setInternalTimer(0.0);
        if (this.freezeLaneOnTrigger) {
            int targetRow = user.getLocation().y();
            for (Zombie zombie : session.getZombies()) {
                if (zombie.isAlive() && (int) zombie.getPosition().y() == targetRow) {
                    zombie.setStatus(Zombie.Status.FREEZE);
                }
            }
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        if (this.resetLifespanTag != null) {
            GameSession session = com.ussr.pvz.model.App.getGameSession();
            if (session.getPlants() != null) {
                for (Plant sibling : session.getPlants()) {
                    if (sibling.isAlive() && sibling.getTags().contains(this.resetLifespanTag)) {
                        sibling.setInternalTimer(sibling.getActionInterval());
                    }
                }
            }
        }
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        user.setInternalTimer(user.getIntervalTimer() - deltaTime);

        if (user.getIntervalTimer() <= 0) {
            user.setInternalTimer(this.fireRate);

            List<Vec2> vectors = (temporaryVectors != null && !temporaryVectors.isEmpty())
                    ? temporaryVectors
                    : user.getShootingVectors();
            if (vectors == null || vectors.isEmpty()) return;

            HitEffectStrategy hitEffect = buildHitEffect(user);

            for (Vec2 direction : vectors) {
                Vec2 velocity = direction.normalize().scale(25.0);

                boolean fireGiant = (user.getPlantFoodTimer() <= fireRate * giantPeaCount) && (giantPeaCount > 0);
                int baseDamage = user.getDamage();
                int finalDamage = fireGiant ? (int) (baseDamage * giantPeaMultiplier) : baseDamage;

                session.getProjectiles().add(new Projectile(
                        user.getPosition(),
                        velocity,
                        null,
                        finalDamage,
                        new StraightMove(),
                        hitEffect
                ));
            }
        }
    }

    private HitEffectStrategy buildHitEffect(Plant user) {
        if (user.getTags().contains(Tag.FIRE)) return new FireHit(1);
        if (user.getTags().contains(Tag.ICE)) return new IceHit(1);
        if (user.getTags().contains(Tag.POISON)) return new PoisonHit(1);
        return new NormalHit(1);
    }
}
