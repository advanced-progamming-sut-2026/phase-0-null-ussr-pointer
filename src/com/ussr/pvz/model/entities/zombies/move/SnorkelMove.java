package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieActivity;
import com.ussr.pvz.model.entities.zombies.Vulnerability;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

// TODO(snorkel-plant-whitelist): submerged-damage rules currently aren't checked against
//  zombies.json's DamageWhileSubmerged / DamageWhileSubmergedPlantfoodOnly per-plant whitelists at
//  all — that logic needs to live wherever incoming damage is resolved against
//  Vulnerability.SUBMERGED (not in this move class, but flagging here since this is where the
//  submerged state is set).
public class SnorkelMove implements MoveBehavior {

    @Override
    public void move(Zombie zombie, GameSession session) {
        Vec2 pos = zombie.getPosition();
        if (pos == null) return;

        double deltaX = zombie.getSpeed().x() * GameClock.SECONDS_PER_TICK;
        double targetX = pos.x() + deltaX;
        zombie.setPosition(Vec2.of(targetX, pos.y()));

        Level level = session.getLevel();
        boolean inWaterSection = level != null && targetX >= level.getCurrentTideColumn();
        boolean isEating = zombie.getState().equals(ZombieActivity.EATING);

        if (inWaterSection && !isEating) {
            // Set tactical rule state to handle Lobber check bypass
            zombie.setVulnerabilityState(Vulnerability.SUBMERGED);
        } else {
            // Surface unit when outside pool lanes or actively chewing a plant
            zombie.setVulnerabilityState(Vulnerability.FULLY_VULNERABLE);
        }

        if (zombie.getPosition().x() < 0) {
            session.onZombieReachedEnd();
        }
    }
}