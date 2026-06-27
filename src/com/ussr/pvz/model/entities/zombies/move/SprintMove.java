package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class SprintMove implements MoveBehavior {
    private final double baseSprintMultiplier;

    public SprintMove() {
        this.baseSprintMultiplier = 1.0;
    }

    public SprintMove(double baseSprintMultiplier) {
        this.baseSprintMultiplier = baseSprintMultiplier;
    }

    @Override
    public void move(Zombie zombie, GameSession session) {
        Vec2 pos = zombie.getPosition();
        if (pos == null) return;

        double activeSpeedX = getActiveSpeedX(zombie);
        double deltaX = activeSpeedX * GameClock.SECONDS_PER_TICK;
        double targetX = pos.x() + deltaX;

        zombie.setPosition(Vec2.of(targetX, pos.y()));

        if (zombie.getPosition().x() < 0) {
            session.onZombieReachedEnd();
        }
    }

    private double getActiveSpeedX(Zombie zombie) {
        double currentMultiplier = this.baseSprintMultiplier;

        if ("ZombieNewspaper".equals(zombie.getAlias())) {
            if (zombie.getArmor() == null || zombie.getArmor().getArmorHp() <= 0) {
                currentMultiplier = 4.0;
            }
        }

        return zombie.getSpeed().x() * currentMultiplier;
    }
}