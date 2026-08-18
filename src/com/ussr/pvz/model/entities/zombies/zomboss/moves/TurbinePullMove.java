package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.SmoothMoveTickable;
import com.ussr.pvz.model.engine.Tickable;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class TurbinePullMove implements ZombossMove {
    private static final int LETHAL_DAMAGE = 99999;
    private static final double PULL_DURATION_SECONDS = 4.0;

    @Override
    public void execute(ZombossController controller, GameSession session) {
        List<Integer> occupiedRows = controller.getOccupiedRows();
        double mouthX = controller.getPrimary().getPosition().x();

        List<Plant> plantsToPull = new ArrayList<>();
        for (Plant plant : session.getPlants()) {
            if (!plant.isAlive()) continue;
            int row = (int) plant.getLocation().y();
            if (occupiedRows.contains(row)) {
                plantsToPull.add(plant);
            }
        }

        for (Plant plant : plantsToPull) {
            int row = plant.getLocation().y();
            int col = plant.getLocation().x();
            Cell cell = session.getLawn().getCell(row, col);
            // Detach from the grid immediately so it can no longer be targeted,
            // fed, or overlap a new planting, but keep it in session.getPlants()
            // (and alive) so it keeps rendering while it's dragged toward the mouth.
            if (cell != null && cell.getPlant() == plant) {
                cell.setPlant(null);
            }

            session.registerTickable(new PullWatcher(
                    new SmoothMoveTickable(plant, Vec2.of(mouthX, row), PULL_DURATION_SECONDS),
                    () -> {
                        plant.takeDamage(LETHAL_DAMAGE);
                        session.getPlants().remove(plant);
                    }
            ));
        }

        for (Zombie zombie : session.getZombies()) {
            if (controller.isBodyOf(zombie)) continue;
            if (!zombie.isAlive()) continue;

            int row = (int) zombie.getPosition().y();
            if (occupiedRows.contains(row)) {
                session.registerTickable(new PullWatcher(
                        new SmoothMoveTickable(zombie, Vec2.of(mouthX, row), PULL_DURATION_SECONDS),
                        () -> zombie.takeDamage(LETHAL_DAMAGE, controller.getPrimary())
                ));
            }
        }
    }

    /**
     * Wraps a SmoothMoveTickable so the "arrival" logic (kill/despawn) fires
     * exactly once, after the pull animation finishes rather than instantly.
     */
    private static class PullWatcher implements Tickable {
        private final SmoothMoveTickable inner;
        private final Runnable onArrived;
        private boolean notified = false;

        PullWatcher(SmoothMoveTickable inner, Runnable onArrived) {
            this.inner = inner;
            this.onArrived = onArrived;
        }

        @Override
        public void update(float delta) {
            inner.update(delta);
            if (inner.isFinished() && !notified) {
                notified = true;
                onArrived.run();
            }
        }
    }
}