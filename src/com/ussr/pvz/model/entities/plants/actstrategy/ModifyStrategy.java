package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.FireHit;
import com.ussr.pvz.model.entities.projectiles.hit.IceHit;
import com.ussr.pvz.model.entities.projectiles.hit.PoisonHit;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.effect.FireEffect;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;

public class ModifyStrategy implements ActStrategy {
    private int damageMultiplier = 1;

    public ModifyStrategy(int damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public void act(Plant user, GameSession session) {
        switch ((int) user.getAbilityValue()) {
            case 1:
                return;
            case 2:
                ArrayList<Projectile> targets = projectileThroughDetect(user, session);
                modifyTargets(user, targets);
                break;
            case 3:
//                ArrayList<Projectile> hypnoTargets = projectileThroughDetect(user, session);
//                applyHypnoModification(hypnoTargets);
                break;
        }
    }

    private ArrayList<Projectile> projectileThroughDetect(Plant user, GameSession session) {
        Vec2 userPos = user.getPosition();
        ArrayList<Projectile> targets = new ArrayList<>();
        for (Projectile projectile : session.getProjectiles()) {
            Vec2 projPos = projectile.getPosition();
            if (Math.abs(userPos.distanceTo(projPos)) < 0.7)
                targets.add(projectile);
        }
        return targets;
    }

    private void modifyTargets(Plant user, ArrayList<Projectile> targets) {
        if (user.getTags().contains(Tag.FIRE)) {
            for (Projectile projectile : targets) {
                if (projectile.getHitEffectStrategy() instanceof FireHit)
                    continue;
                projectile.setHitEffectStrategy(new FireHit(1));
            }
        } else if (user.getTags().contains(Tag.ICE)) {
            for (Projectile projectile : targets) {
                if (projectile.getHitEffectStrategy() instanceof IceHit)
                    continue;
                projectile.setHitEffectStrategy(new IceHit(1));
            }
        } else if (user.getTags().contains(Tag.POISON)) {
            for (Projectile projectile : targets) {
                if (projectile.getHitEffectStrategy() instanceof PoisonHit)
                    continue;
                projectile.setHitEffectStrategy(new PoisonHit(1));
            }
        }
        for (Projectile projectile : targets) {
            projectile.setDamage(projectile.getDamage() * damageMultiplier);
        }
    }

    public void setDamageMultiplier(int multiplier) {
        this.damageMultiplier = multiplier;
    }

    private void applyHypnoModification(ArrayList<Projectile> targets) {
        for (Projectile projectile : targets) {
            // Intercept and rewrite the projectile hit behavior to trigger hypnosis on contact
            projectile.setHitEffectStrategy(new com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy() {
                @Override
                public void apply(ArrayList<com.ussr.pvz.model.engine.GameEntity> entities, Projectile proj) {
                    proj.setAlive(false);
                    for (com.ussr.pvz.model.engine.GameEntity entity : entities) {
                        if (entity instanceof Zombie zombie && zombie.isAlive()) {
                            zombie.setStatus(Zombie.Status.HYPNOTIZED);
                            zombie.hypnotize();
                        }
                    }
                }

                @Override
                public int getAreaLength() {
                    return 1;
                }
            });
        }
    }
}
