package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;

public class DeadlineBehavior extends LevelBehavior {

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);

        if (levelCompleted || session.isGameOver()) return;

        Level level = session.getLevel();
        if (level == null) return;

        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive() && zombie.getPosition().x() < level.getDeadlineColumn()) {
                session.getEventBus().publish(new GameEvent.GameOver());
                break;
            }
        }
    }
}