package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.hit.*;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;
import java.util.List;

public class LobberBarrage implements PlantFoodEffect {

    public LobberBarrage() {
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (user == null || session == null || session.getZombies() == null) return;

        List<Zombie> targetsInLine = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive() && isInSameLine(user, zombie)) {
                targetsInLine.add(zombie);
            }
        }

        if (targetsInLine.isEmpty()) return;

        HitEffectStrategy hitEffect = handleHitEffect(user);

        for (Zombie target : targetsInLine) {
            target.takeDamage(user.getDamage());
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

    private boolean isInSameLine(Plant user, Zombie zombie) {
        return (Math.abs(user.getPosition().y() - zombie.getPosition().y()) < 0.5);
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