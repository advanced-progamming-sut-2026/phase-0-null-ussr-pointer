package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class SprintMove implements MoveBehavior {
    private final double baseSprintMultiplier;
    private final double enrageMultiplier;
    private final boolean enragesOnArmorLoss;

    public SprintMove() {
        this(1.0, 1.0, false);
    }

    public SprintMove(double baseSprintMultiplier) {
        this(baseSprintMultiplier, 1.0, false);
    }

    public SprintMove(double baseSprintMultiplier, double enrageMultiplier, boolean enragesOnArmorLoss) {
        this.baseSprintMultiplier = baseSprintMultiplier;
        this.enrageMultiplier = enrageMultiplier;
        this.enragesOnArmorLoss = enragesOnArmorLoss;
    }

    @Override
    public void move(Zombie zombie, GameSession session) {
        Vec2 pos = zombie.getPosition();
        if (pos == null) return;

        double deltaX = getActiveSpeedX(zombie) * GameClock.SECONDS_PER_TICK;
        Vec2 newPos = Vec2.of(pos.x() + deltaX, pos.y());

        int oldCol = (int) pos.x();
        int newCol = (int) newPos.x();
        if (newCol != oldCol) {
            newPos = applySlipperyShift(newPos, session);
        }

        zombie.setPosition(newPos);

        if (zombie.getPosition().x() < 0) {
            session.onZombieReachedEnd();
        }
    }

    protected double getActiveSpeedX(Zombie zombie) {
        if (enragesOnArmorLoss && (zombie.getArmor() == null || zombie.getArmor().isDestroyed())) {
            return zombie.getSpeed().x() * enrageMultiplier;
        }
        return zombie.getSpeed().x() * baseSprintMultiplier;
    }
}