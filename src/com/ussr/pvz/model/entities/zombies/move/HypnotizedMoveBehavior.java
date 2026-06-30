package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class HypnotizedMoveBehavior implements MoveBehavior {

    private final MoveBehavior delegate;

    public HypnotizedMoveBehavior(MoveBehavior delegate) {
        this.delegate = delegate;
    }

    @Override
    public void move(Zombie zombie, GameSession session) {
        delegate.move(zombie, session);

        if (hasWalkedOffTheRightEdge(zombie, session)) {
            zombie.setAlive(false);
        }
    }

    private boolean hasWalkedOffTheRightEdge(Zombie zombie, GameSession session) {
        return zombie.getPosition() != null
                && session.getLawn() != null
                && zombie.getPosition().x() >= session.getLawn().getCols();
    }
}