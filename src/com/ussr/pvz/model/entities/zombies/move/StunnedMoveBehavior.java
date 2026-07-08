package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class StunnedMoveBehavior implements MoveBehavior {
    private final MoveBehavior delegate;
    private double stunTimer;

    public StunnedMoveBehavior(MoveBehavior delegate, double stunDuration) {
        this.delegate = delegate;
        this.stunTimer = stunDuration;
    }

    @Override
    public void move(Zombie zombie, GameSession session) {
        stunTimer -= GameClock.SECONDS_PER_TICK;

        if (stunTimer <= 0) {
            // Restore the original behavior when the stun wears off
            zombie.setMoveBehavior(delegate);
        }
        // By doing nothing else here, the zombie is effectively immobilized
    }
}