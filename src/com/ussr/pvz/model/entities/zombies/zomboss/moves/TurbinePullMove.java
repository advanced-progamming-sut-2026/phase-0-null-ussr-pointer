package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.SmoothMoveTickable;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class TurbinePullMove implements ZombossMove {
    private static final double PULL_DURATION_SECONDS = 0.6;

    @Override
    public void execute(ZombossController controller, GameSession session) {
        List<Integer> occupiedRows = controller.getOccupiedRows();

        List<Plant> plantsToRemove = new ArrayList<>();
        for (Plant plant : session.getPlants()) {
            int row = (int) plant.getLocation().y();
            if (occupiedRows.contains(row)) {
                plantsToRemove.add(plant);
            }
        }

        for (Plant plant : plantsToRemove) {
            removePlant(plant, session);
        }

        for (Zombie zombie : session.getZombies()) {
            if (controller.isBodyOf(zombie)) continue;
            int row = (int) zombie.getPosition().y();
            if (occupiedRows.contains(row)) {
                Vec2 target = Vec2.of(controller.getPrimary().getPosition().x(), row);
                session.registerTickable(new SmoothMoveTickable(zombie, target, PULL_DURATION_SECONDS));
            }
        }
    }

    private void removePlant(Plant plant, GameSession session) {
        int col = (int) plant.getLocation().x();
        int row = (int) plant.getLocation().y();
        Cell cell = session.getLawn().getCell(row, col);

        if (cell != null && cell.getPlant() == plant) {
            session.removePlantAt(col, row);
            return;
        }

        plant.setAlive(false);
        session.getPlants().remove(plant);
        session.notifyPlantPlucked(plant);

        if (cell != null) {
            InteractableStructure structure = cell.getInteractableStructure();
            if (structure != null) {
                structure.setAlive(false);
            }
        }
    }
}