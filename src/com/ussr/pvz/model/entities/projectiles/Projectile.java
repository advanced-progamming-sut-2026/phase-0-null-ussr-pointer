package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
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
    private int damage;
    private Damageable target;
    private boolean isStunning;

    private MoveStrategy moveStrategy;
    private HitEffectStrategy hitEffectStrategy;

    public Projectile(Vec2 position, Vec2 velocity, Zombie zombie, int damage, MoveStrategy moveStrategy, HitEffectStrategy hitEffectStrategy) {
        this((Damageable) zombie, position, velocity, damage, moveStrategy, hitEffectStrategy);
    }

    public Projectile(Damageable target, Vec2 position, Vec2 velocity, int damage, MoveStrategy moveStrategy, HitEffectStrategy hitEffectStrategy) {
        this.setPosition(position);
        this.setSpeed(velocity);
        this.target = target;
        this.damage = damage;
        this.moveStrategy = moveStrategy;
        this.hitEffectStrategy = hitEffectStrategy;
        this.isStunning = false;
        if(moveStrategy instanceof ArcMove) {
            ((ArcMove) moveStrategy).setGroundY(position.y());
        }
        else if(moveStrategy instanceof StraightMove) {
            ((StraightMove) moveStrategy).setSpeedMagnitude(velocity.length());
        }
        else if(moveStrategy instanceof BounceMove) {
            ((BounceMove) moveStrategy).setSpeedMagnitude(velocity.length());
        }
        moveStrategy.initialize(this , target);
    }

    public void setStunning(boolean isStunning) {
        this.isStunning = isStunning;
    }

    @Override
    public void tick() {
        if (!isAlive) return;

        if (moveStrategy != null) {
            moveStrategy.move(this);
        }
        else {
            this.isAlive = false;
        }

        ArrayList<GameEntity> targets;
        if (moveStrategy instanceof ArcMove) {
            targets = checkArcCollision();
        } else {
            targets = checkCollision();
        }

        if(targets != null && targets.isEmpty())
            return ;
        if(hitEffectStrategy != null)
            hitEffectStrategy.apply(targets , this);
        else
            this.isAlive = false;
        if (moveStrategy instanceof BounceMove bounceMove) {
            bounceMove.bounce(this);
            this.isAlive = true;
        }
    }

    private ArrayList<GameEntity> checkCollision() {
        GameSession session = App.getGameSession();
        if (session == null) return null;

        GameEntity physicalImpactTarget = null;

        ArrayList<InteractableStructure> interactableStructures = session.getLawn().getAllInteractable();
        for (InteractableStructure structure : interactableStructures) {
            if (!structure.isAlive()) continue;
            if (this.getPosition().distanceTo(structure.getPosition()) < 0.2) {
                physicalImpactTarget = structure;
                break;
            }
        }

        if (physicalImpactTarget == null) {
            List<Zombie> zombies = session.getZombies();
            for (Zombie zombie : zombies) {
                if (!zombie.isAlive()) continue;
                if (this.getPosition().distanceTo(zombie.getPosition()) < 0.2) {
                    physicalImpactTarget = zombie;
                    break;
                }
            }
        }

        if (physicalImpactTarget == null) {
            return null;
        }

        return targetFinder(interactableStructures , session);

    }

    public ArrayList<GameEntity> targetFinder(ArrayList<InteractableStructure> interactableStructures, GameSession session) {
        ArrayList<GameEntity> targets = new ArrayList<>();

        int areaLength = hitEffectStrategy.getAreaLength();
        double straightDist = (int) (areaLength / 2) + 0.2;
        if(areaLength == 1)
            straightDist = 0.2;

        Vec2 explosionEpicenter = this.getPosition();

        for (InteractableStructure structure : interactableStructures) {
            if (!structure.isAlive()) continue;
            Vec2 pos = structure.getPosition();
            if (Math.abs(explosionEpicenter.y() - pos.y()) < straightDist && Math.abs(explosionEpicenter.x() - pos.x()) < straightDist) {
                targets.add(structure);
            }
        }

        List<Zombie> zombies = session.getZombies();
        for (Zombie zombie : zombies) {
            if (!zombie.isAlive()) continue;
            Vec2 pos = zombie.getPosition();
            if (Math.abs(explosionEpicenter.y() - pos.y()) < straightDist && Math.abs(explosionEpicenter.x() - pos.x()) < straightDist) {
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

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) { this.damage = damage; }
}