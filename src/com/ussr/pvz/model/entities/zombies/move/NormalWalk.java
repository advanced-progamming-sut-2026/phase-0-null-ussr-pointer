package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class NormalWalk implements MoveBehavior {

    @Override
    public void move(
            Zombie zombie,
            GameSession session,
            float delta
    ) {
        Vec2 position = zombie.getPosition();
        Vec2 velocity = zombie.getSpeed();

        if (position == null || velocity == null) {
            return;
        }

        Vec2 newPosition = position.add(
                velocity.scale(delta)
        );

        zombie.setPosition(
                applySlipperyShift(newPosition, session)
        );
    }
}