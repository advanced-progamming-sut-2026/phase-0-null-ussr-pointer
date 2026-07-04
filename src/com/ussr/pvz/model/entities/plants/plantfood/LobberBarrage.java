package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.*;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LobberBarrage implements PlantFoodEffect{
    private final int damage;
    private final double splashRadius;
    private final int maxTargets; // -1 for all zombies in the pitch

    /**
     * Unified constructor to handle all high-arcing plant food barrages.
     *
     * @param damage             Direct impact damage of the ultimate projectile.
     * @param splashRadius       The radius of the splash damage zone (0.0 for single-target).
     * @param maxTargets         Maximum targets to hit. Use -1 to target every single alive zombie on the board.
     */

    public LobberBarrage(int damage, double splashRadius, int maxTargets) {
        this.damage = damage;
        this.splashRadius = splashRadius;
        this.maxTargets = maxTargets;
    }


    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (session.getZombies() == null || session.getZombies().isEmpty()) return;

        List<Zombie> targetsToHit = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive()) {
                targetsToHit.add(zombie);
            }
        }

        if (targetsToHit.isEmpty()) return;

        if (this.maxTargets > -1) {
            Collections.shuffle(targetsToHit);
            int count = Math.min(this.maxTargets, targetsToHit.size());
            targetsToHit = targetsToHit.subList(0, count);
        }


        Vec2 spawnPosition = new Vec2(user.getLocation().x(), user.getLocation().y());
        Vec2 velocity = new Vec2(20 , 20);

        for (Zombie target : targetsToHit) {
            HitEffectStrategy hitEffectStrategy = handleHitEffect(user);
            Projectile projectile = new Projectile(
                    spawnPosition,
                    velocity,
                    target,
                    this.damage,
                    new ArcMove(9.8),
                    hitEffectStrategy
            );

            session.getProjectiles().add(projectile);
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {

    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {

    }

    private HitEffectStrategy handleHitEffect(Plant user) {
        if(user.getTags().contains(Tag.ICE))
            return new IceHit((int) splashRadius);
        if(user.getTags().contains(Tag.FIRE))
            return new FireHit((int) splashRadius);
        if(user.getTags().contains(Tag.POISON))
            return new PoisonHit((int) splashRadius);
        if(user.getTags().contains(Tag.BUTTER))
            return new ButterHit((int) splashRadius);
        return new NormalHit((int) splashRadius);
    }
}
