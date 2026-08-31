package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class HypnotizedMoveBehavior implements MoveBehavior {

    private final MoveBehavior delegate;

    public HypnotizedMoveBehavior(MoveBehavior delegate) {
        this.delegate = delegate;
    }

    @Override
    public void move(Zombie zombie, GameSession session, float delta) {
        delegate.move(zombie, session, delta);

        if (hasWalkedOffTheRightEdge(zombie, session)) {
            killGracefully(zombie, session);
        }
    }

    private boolean hasWalkedOffTheRightEdge(Zombie zombie, GameSession session) {
        return zombie.getPosition() != null
                && session.getLawn() != null
                && zombie.getPosition().x() > session.getLawn().getCols();
    }

    /**
     * Triggers the proper death pipeline instead of just toggling isAlive.
     * This ensures the death animation plays, loot is rolled, and quest/event
     * listeners fire — identical to what happens when a zombie is killed by a plant.
     */
    private void killGracefully(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) return;
        zombie.setAlive(false);
        zombie.startDeathTimer();
        session.notifyZombieDied(zombie);
    }
}