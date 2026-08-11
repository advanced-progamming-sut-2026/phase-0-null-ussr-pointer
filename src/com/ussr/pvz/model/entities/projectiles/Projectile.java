package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.projectiles.move.BounceMove;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class Projectile extends GameEntity {
    private static final float VISUAL_LAUNCH_BLEND_SECONDS = 0.25f;

    private int damage;
    private Damageable target;
    private boolean isStunning;
    private final Plant user;
    private Vec2 previousPosition;
    private double visualHeight;
    private Vec2 visualLaunchOrigin = Vec2.of(0.5, 0.0);
    private float visualAge;

    private MoveStrategy moveStrategy;
    private HitEffectStrategy hitEffectStrategy;

    public Projectile(Vec2 position, Vec2 velocity, Zombie zombie, int damage, MoveStrategy moveStrategy,
                      HitEffectStrategy hitEffectStrategy, Plant user) {
        this(zombie, position, velocity, damage, moveStrategy, hitEffectStrategy, user);
    }

    public Projectile(Damageable target, Vec2 position, Vec2 velocity, int damage, MoveStrategy moveStrategy,
                      HitEffectStrategy hitEffectStrategy, Plant user) {
        this.setPosition(position);
        this.setSpeed(velocity);
        this.target = target;
        this.damage = damage;
        this.moveStrategy = moveStrategy;
        this.hitEffectStrategy = hitEffectStrategy;
        this.isStunning = false;
        this.user = user;
        if (user != null) {
            this.visualLaunchOrigin = user.getProjectileOrigin(0);
        }
        if (moveStrategy != null) {
            switch (moveStrategy) {
                case ArcMove arcMove -> arcMove.setGroundY(position.y());
                case StraightMove straightMove -> straightMove.setSpeedMagnitude(velocity.length());
                case BounceMove bounceMove -> bounceMove.setSpeedMagnitude(velocity.length());
                default -> {
                }
            }
            moveStrategy.initialize(this, target);
        }
    }

    public void setStunning(boolean isStunning) {
        this.isStunning = isStunning;
    }

    public double getVisualHeight() {
        return visualHeight;
    }

    public void setVisualHeight(double visualHeight) {
        this.visualHeight = Math.max(0.0, visualHeight);
    }

    public Vec2 getVisualLaunchOrigin() {
        return visualLaunchOrigin;
    }

    public void setVisualLaunchOrigin(Vec2 visualLaunchOrigin) {
        if (visualLaunchOrigin != null) {
            this.visualLaunchOrigin = visualLaunchOrigin;
        }
    }

    public float getVisualLaunchBlend() {
        return Math.max(
                0f,
                1f - visualAge / VISUAL_LAUNCH_BLEND_SECONDS
        );
    }

    @Override
    public void update(float delta) {
        if (!isAlive) return;

        visualAge += delta;

        previousPosition = getPosition();

        if (moveStrategy != null) {
            moveStrategy.move(this, delta);
        } else {
            this.isAlive = false;
        }

        ArrayList<GameEntity> targets;
        if (moveStrategy instanceof ArcMove) {
            targets = checkArcCollision();
        } else {
            targets = checkCollision();
        }

        if (targets == null || targets.isEmpty())
            return;
        if (hitEffectStrategy != null)
            hitEffectStrategy.apply(targets, this);
        else
            this.isAlive = false;
//        if (moveStrategy instanceof BounceMove bounceMove) {
//            bounceMove.bounce(this);
//            this.isAlive = true;
//        }
    }

    private ArrayList<GameEntity> checkCollision() {
        GameSession session = App.getGameSession();
        if (session == null) return null;

        if (target instanceof Plant) {
            return checkPlantCollision(session);
        }

        GameEntity physicalImpactTarget = null;

        ArrayList<InteractableStructure> interactableStructures = session.getLawn().getAllInteractable();
        for (InteractableStructure structure : interactableStructures) {
            if (!structure.isAlive()) continue;
            if (structure.getPosition() == null) continue;
            if (hitEffectStrategy != null && !hitEffectStrategy.canHit(structure)) continue;
            if (crossedEntity(structure.getPosition(), 0.2)) {
                physicalImpactTarget = structure;
                break;
            }
        }

        if (physicalImpactTarget == null) {
            List<Zombie> zombies = session.getZombies();
            for (Zombie zombie : zombies) {
                if (!zombie.isAlive()) continue;
                if (hitEffectStrategy != null && !hitEffectStrategy.canHit(zombie)) continue;
                if (crossedEntity(zombie.getPosition(), 0.2)) {
                    if (zombie.getDefenseBehavior() instanceof com.ussr.pvz.model.entities.zombies.defense
                            .JesterDefense jester) {
                        if (this.getMoveStrategy() instanceof StraightMove || this.getMoveStrategy()
                                instanceof ArcMove) {
                            this.setSpeed(this.getSpeed().scale(-1));
                            this.target = jester.findNearestPlantInLane(zombie, session);
                            jester.triggerSpin(zombie);

                            return new ArrayList<>();
                        }
                    }

                    physicalImpactTarget = zombie;
                    break;
                }
            }
        }

        if (physicalImpactTarget == null) {
            return null;
        }

        if (physicalImpactTarget.getPosition() != null
                && (hitEffectStrategy == null || !hitEffectStrategy.continuesAfterHit())) {
            setPosition(physicalImpactTarget.getPosition());
        }
        if (hitEffectStrategy != null && hitEffectStrategy.continuesAfterHit()) {
            ArrayList<GameEntity> targets = new ArrayList<>();
            targets.add(physicalImpactTarget);
            return targets;
        }
        return targetFinder(interactableStructures, session);
    }

    private boolean crossedEntity(Vec2 entityPosition, double radius) {
        Vec2 currentPosition = getPosition();
        if (currentPosition == null || entityPosition == null) return false;
        if (previousPosition == null) {
            return currentPosition.distanceTo(entityPosition) < radius;
        }

        double dx = currentPosition.x() - previousPosition.x();
        double dy = currentPosition.y() - previousPosition.y();
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) {
            return currentPosition.distanceTo(entityPosition) < radius;
        }

        double projection = ((entityPosition.x() - previousPosition.x()) * dx
                + (entityPosition.y() - previousPosition.y()) * dy) / lengthSquared;
        projection = Math.max(0.0, Math.min(1.0, projection));
        Vec2 closestPoint = Vec2.of(
                previousPosition.x() + projection * dx,
                previousPosition.y() + projection * dy
        );
        return closestPoint.distanceTo(entityPosition) < radius;
    }

    private ArrayList<GameEntity> checkPlantCollision(GameSession session) {
        if (!(target instanceof Plant targetPlant) || !targetPlant.isAlive() || targetPlant.getLocation() == null) {
            return null;
        }
        if (session.getPlants() == null) return null;

        Vec2 targetPos = Vec2.of(targetPlant.getLocation().x(), targetPlant.getLocation().y());
        Vec2 pos = this.getPosition();
        Vec2 speed = this.getSpeed();

        boolean withinRadius = pos.distanceTo(targetPos) < 0.5;
        boolean crossedX = speed != null && ((speed.x() < 0 && pos.x() <= targetPos.x())
                || (speed.x() > 0 && pos.x() >= targetPos.x()));

        if (!withinRadius && !crossedX) {
            return null;
        }

        ArrayList<GameEntity> targets = new ArrayList<>();
        int areaLength = hitEffectStrategy.getAreaLength();
        double straightDist = (int) (areaLength / 2) + 0.2;
        if (areaLength == 1) straightDist = 0.2;

        for (Plant plant : session.getPlants()) {
            if (plant == null || !plant.isAlive() || plant.getLocation() == null) continue;
            Vec2 plantPos = Vec2.of(plant.getLocation().x(), plant.getLocation().y());
            if (Math.abs(pos.y() - plantPos.y()) < straightDist && Math.abs(pos.x() - plantPos.x()) < straightDist) {
                targets.add(plant);
            }
        }

        if (targets.isEmpty()) {
            targets.add(targetPlant);
        }

        return targets;
    }

    public ArrayList<GameEntity> targetFinder(ArrayList<InteractableStructure> interactableStructures,
                                              GameSession session) {
        ArrayList<GameEntity> targets = new ArrayList<>();

        int areaLength = hitEffectStrategy.getAreaLength();
        double straightDist = (int) (areaLength / 2) + 0.2;
        if (areaLength == 1)
            straightDist = 0.2;

        Vec2 explosionEpicenter = this.getPosition();

        for (InteractableStructure structure : interactableStructures) {
            if (!structure.isAlive()) continue;
            Vec2 pos = structure.getPosition();
            if (pos == null) continue;
            if (Math.abs(explosionEpicenter.y() - pos.y()) < straightDist
                    && Math.abs(explosionEpicenter.x() - pos.x()) < straightDist) {
                targets.add(structure);
            }
        }

        List<Zombie> zombies = session.getZombies();
        for (Zombie zombie : zombies) {
            if (!zombie.isAlive()) continue;
            Vec2 pos = zombie.getPosition();
            if (Math.abs(explosionEpicenter.y() - pos.y()) < straightDist
                    && Math.abs(explosionEpicenter.x() - pos.x()) < straightDist) {
                targets.add(zombie);
            }
        }

        return targets;
    }

    private ArrayList<GameEntity> checkArcCollision() {
        if (!(moveStrategy instanceof ArcMove arcMove)) return null;

        if (!arcMove.hasLanded()) {
            return null;
        }

        GameSession session = App.getGameSession();
        if (session == null) return null;

        ArrayList<InteractableStructure> interactableStructures = session.getLawn().getAllInteractable();

        ArrayList<GameEntity> targets = targetFinder(interactableStructures, session);

        if (targets.isEmpty()) {
            this.isAlive = false;
        }

        return targets;
    }

    public void setHitEffectStrategy(HitEffectStrategy strategy) {
        this.hitEffectStrategy = strategy;
    }

    public HitEffectStrategy getHitEffectStrategy() {
        return this.hitEffectStrategy;
    }

    public Object getMoveStrategy() {
        return moveStrategy;
    }

    public void notifyTargetHit(Damageable hitTarget) {
        if (moveStrategy != null && hitTarget != null) {
            moveStrategy.onTargetHit(this, hitTarget);
        }
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public boolean isStunning() {
        return isStunning;
    }

    public Plant getUser() {
        return user;
    }
}
