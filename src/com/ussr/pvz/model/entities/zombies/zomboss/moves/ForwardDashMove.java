package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

/**
 * Egypt Zomboss "ForwardDash": the boss suddenly lunges forward across its
 * two occupied rows, destroying every plant in its path, then returns to
 * its original spot.
 *
 * The dash distance is now derived from the boss's current x position
 * instead of a fixed small offset, so it visibly travels across the whole
 * row (rather than twitching a tile or so) before snapping back. Duration
 * is derived from distance / DASH_SPEED so the dash always looks like it's
 * moving at the same speed no matter where on the lawn the boss currently
 * sits.
 */
public class ForwardDashMove implements ZombossMove {
    // Tiles per second the boss covers while dashing out / returning.
    private static final double DASH_SPEED = 10.0;
    private static final double DASH_HOLD_SECONDS = 0.2;
    private static final double MIN_DASH_SECONDS = 0.25;
    // Return trip is slightly slower than the lunge out, for a bit of weight.
    private static final double RETURN_SPEED_SCALE = 0.85;

    @Override
    public void execute(ZombossController controller, GameSession session) {
        for (int row : controller.getOccupiedRows()) {
            if (row >= session.getLawn().getRows()) continue;
            for (int col = 0; col < session.getLawn().getCols(); col++) {
                session.removePlantAt(col, row);
            }
        }

        double currentX = controller.getPrimary().getPosition().x();
        // Distance to cross the entire row: from the boss's current x all
        // the way to the far edge of the lawn (column 0), with a fallback
        // to the full lawn width in case position tracking is off/zero.
        double distance = Math.max(currentX, session.getLawn().getCols());

        double outDuration = Math.max(MIN_DASH_SECONDS, distance / DASH_SPEED);
        double returnDuration = Math.max(MIN_DASH_SECONDS, distance / (DASH_SPEED * RETURN_SPEED_SCALE));

        controller.startDash(distance, outDuration, DASH_HOLD_SECONDS, returnDuration);
    }
}