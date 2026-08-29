package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.plants.upgrades.SpecialUpgrade;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.*;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;
import com.badlogic.gdx.math.MathUtils;


public class LobberStrategy implements ActStrategy {
    private static final double GRAVITY = 8;
    private static final double HORIZONTAL_SPEED = 4.0;

    @Override
    public void act(Plant user, GameSession session) {
        GameEntity target = findTargetInLane(user, session);
        if (target == null) return;

        Vec2 startPos = user.getPosition();
        Vec2 targetPos = target.getPosition();
        double distanceX = targetPos.x() - startPos.x();
        double timeOfFlight = distanceX / HORIZONTAL_SPEED;
        double initialVelocityY = -0.5 * GRAVITY * timeOfFlight;

        Vec2 initialVelocity = new Vec2(HORIZONTAL_SPEED, initialVelocityY);
        HitEffectStrategy hitEffect = buildHitEffect(user);
        Projectile projectile = new Projectile(
                (Damageable) target,
                user.getPosition(),
                initialVelocity,
                user.getDamage(),
                new ArcMove(GRAVITY),
                hitEffect, user
        );
        projectile.setVisualLaunchOrigin(user.getProjectileOrigin(0));
        session.addProjectile(projectile);
        user.triggerActionAnimation();
    }


    private HitEffectStrategy buildHitEffect(Plant user) {
        int areaLength = user.getTags().contains(Tag.AOE) ? 3 : 1;
        if (user.getName().equalsIgnoreCase("kernel-pult")) {
            double butterChance = 0.25
                    + user.getSpecialUpgradeValue(SpecialUpgrade.BUTTER_CHANCE_BUFF);
            if (MathUtils.random() < Math.min(1.0, butterChance)) {
                return new ButterHit(areaLength);
            }
        }
        if (user.getTags().contains(Tag.FIRE)) return new FireHit(areaLength);
        if (user.getTags().contains(Tag.ICE)) return new IceHit(areaLength);
        if (user.getTags().contains(Tag.POISON)) {
            return new PoisonHit(areaLength,
                    user.getSpecialUpgradeInt(SpecialUpgrade.POISON_TICK_BUFF));
        }
        return new NormalHit(areaLength);
    }

    private GameEntity findTargetInLane(Plant user, GameSession session) {
        Zombie zombie = findNearestZombieInLane(user, session);
        if (zombie != null) return zombie;
        return findNearestGraveInLane(user, session);
    }

    private Zombie findNearestZombieInLane(Plant user, GameSession session) {
        double plantRow = user.getPosition().y();
        Zombie nearest = null;
        double minX = Double.MAX_VALUE;

        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !zombie.isAlive()) continue;
            Vec2 zp = zombie.getPosition();

            if (Math.abs(zp.y() - plantRow) < 0.5 && zp.x() > user.getPosition().x()) {
                if (zp.x() < minX) {
                    minX = zp.x();
                    nearest = zombie;
                }
            }
        }
        return nearest;
    }

    private Grave findNearestGraveInLane(Plant user, GameSession session) {
        if (session.getLawn() == null) return null;

        double plantRow = user.getPosition().y();
        Grave nearest = null;
        double minX = Double.MAX_VALUE;

        for (InteractableStructure structure : session.getLawn().getAllInteractable()) {
            if (!(structure instanceof Grave grave) || !grave.isAlive()) continue;
            Vec2 gp = grave.getPosition();
            if (gp == null) continue;

            if (Math.abs(gp.y() - plantRow) < 0.5 && gp.x() > user.getPosition().x()) {
                if (gp.x() < minX) {
                    minX = gp.x();
                    nearest = grave;
                }
            }
        }
        return nearest;
    }
}