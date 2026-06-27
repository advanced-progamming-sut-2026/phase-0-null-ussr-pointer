package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieActivity;
import com.ussr.pvz.model.entities.zombies.Vulnerability;
import com.ussr.pvz.model.util.Vec2;

public class SnorkelMove implements MoveBehavior {
    private static final double WATER_START_X = 2.0;
    private static final double WATER_END_X = 7.0;

    @Override
    public void move(Zombie zombie, GameSession session) {
        Vec2 pos = zombie.getPosition();
        if (pos == null) return;

        double deltaX = zombie.getSpeed().x() * GameClock.SECONDS_PER_TICK;
        double targetX = pos.x() + deltaX;
        zombie.setPosition(Vec2.of(targetX, pos.y()));

        boolean inWaterSection = (targetX >= WATER_START_X && targetX <= WATER_END_X);
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