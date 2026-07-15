package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class NormalWalk implements MoveBehavior {

    @Override
    public void move(Zombie zombie, GameSession session) {
        Vec2 pos = zombie.getPosition();
        Vec2 vel = zombie.getSpeed();

        Vec2 newPos = pos.add(vel.scale(GameClock.SECONDS_PER_TICK));

        int oldCol = (int) pos.x();
        int newCol = (int) newPos.x();
        if (newCol != oldCol) {
            newPos = applySlipperyShift(newPos, session);
        }

        zombie.setPosition(newPos);
    }
}