package com.ussr.pvz.model.entities.zombies.targeting;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.Comparator;

public class ZombieSideTargetFinder implements TargetFinder {

    private static final double EATING_RANGE = 0.4;

    @Override
    public Damageable findTarget(Zombie self, GameSession session) {
        Damageable plantOrStructure = findPlantOrStructureAhead(self, session);
        if (plantOrStructure != null) return plantOrStructure;

        return nearestEnemyAhead(self, session);
    }

    private Damageable findPlantOrStructureAhead(Zombie self, GameSession session) {
        if (self.getPosition() == null || session.getLawn() == null) return null;

        int row = (int) self.getPosition().y();
        double myX = self.getPosition().x();

        // Check the cell the zombie is currently in, and the cell immediately ahead (left)
        int currentCol = (int) Math.floor(myX);
        int aheadCol = (int) Math.round(myX - EATING_RANGE);

        Damageable target = checkCellForEdibles(session, row, aheadCol);
        if (target != null) return target;

        return checkCellForEdibles(session, row, currentCol);
    }

    private Damageable checkCellForEdibles(GameSession session, int row, int col) {
        if (col < 0 || col >= session.getLawn().getCols()) return null;
        Cell cell = session.getLawn().getCell(row, col);
        if (cell == null) return null;

        Plant plant = cell.getPlant();
        if (plant != null && plant.isAlive()) {
            return plant;
        }

        InteractableStructure structure = cell.getInteractableStructure();
        if (structure instanceof Damageable damageable && structure.isAlive()) {
            return damageable;
        }

        return null;
    }

    private Zombie nearestEnemyAhead(Zombie self, GameSession session) {
        if (self.getPosition() == null || session.getZombies() == null) return null;

        int row = (int) self.getPosition().y();
        double myX = self.getPosition().x();

        return session.getZombies().stream()
                .filter(other -> other != self && other.isAlive())
                .filter(other -> other.getFaction() == Faction.ZOMBIES)
                .filter(other -> other.getPosition() != null)
                .filter(other -> (int) other.getPosition().y() == row)
                .filter(other -> isInEatingRange(myX, other.getPosition().x()))
                .min(Comparator.comparingDouble(other -> other.getPosition().x() - myX))
                .orElse(null);
    }

    private boolean isInEatingRange(double myX, double otherX) {
        double dist = otherX - myX;
        return dist >= 0 && dist <= EATING_RANGE;
    }
}