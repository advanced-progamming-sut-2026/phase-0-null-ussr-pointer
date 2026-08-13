package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class TurbinePullMove implements ZombossMove {
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
            session.removePlantAt((int) plant.getLocation().x(), (int) plant.getLocation().y());
        }

        for (Zombie zombie : session.getZombies()) {
            if (controller.isBodyOf(zombie)) continue;
            int row = (int) zombie.getPosition().y();
            if (occupiedRows.contains(row)) {
                zombie.setPosition(Vec2.of(controller.getPrimary().getPosition().x(), row));
            }
        }
    }
}