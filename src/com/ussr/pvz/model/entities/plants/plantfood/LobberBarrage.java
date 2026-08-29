package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.*;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class LobberBarrage implements PlantFoodEffect {

    public LobberBarrage() {
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (user == null || session == null) return;

        List<GameEntity> targets = new ArrayList<>();
        if (session.getZombies() != null) {
            for (Zombie zombie : session.getZombies()) {
                if (zombie != null && zombie.isAlive() && isInSameLine(user, zombie)) {
                    targets.add(zombie);
                }
            }
        }

        if (targets.isEmpty() && session.getLawn() != null) {
            for (InteractableStructure structure : session.getLawn().getAllInteractable()) {
                if (structure instanceof Grave grave && grave.isAlive() && isInSameLine(user, grave)) {
                    targets.add(grave);
                }
            }
        }

        if (targets.isEmpty()) return;

        for (GameEntity target : targets) {
            HitEffectStrategy hitEffect = handleHitEffect(user);
            session.addProjectile(new Projectile((Damageable) target,
                    user.getPosition(), new Vec2(3 , 5) ,
                    user.getDamage() * 5 , new ArcMove(8) , hitEffect, user));
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        // Instant superpower trigger; no stat modifiers needed
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        // Instant superpower trigger; no tick duration needed
    }

    private boolean isInSameLine(Plant user, GameEntity entity) {
        return entity.getPosition() != null
                && (Math.abs(user.getPosition().y() - entity.getPosition().y()) < 0.5);
    }

    private HitEffectStrategy handleHitEffect(Plant user) {
        int areaLength = user.getTags().contains(Tag.AOE) ? 3 : 1;
        if (user.getTags().contains(Tag.ICE))
            return new IceHit((int) areaLength);
        if (user.getTags().contains(Tag.FIRE))
            return new FireHit((int) areaLength);
        if (user.getTags().contains(Tag.POISON))
            return new PoisonHit((int) areaLength);
        if (user.getTags().contains(Tag.BUTTER))
            return new ButterHit((int) areaLength);
        return new NormalHit((int) areaLength);
    }
}