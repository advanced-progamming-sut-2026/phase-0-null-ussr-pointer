package com.ussr.pvz.model.entities.zombies.targeting;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.Comparator;

public class ZombieSideTargetFinder implements TargetFinder {

    private static final double EATING_RANGE = 0.5;

    @Override
    public Damageable findTarget(Zombie self, GameSession session) {
        Damageable structure = structureInCurrentCell(self, session);
        if (structure != null) return structure;

        return nearestEnemyAhead(self, session);
    }

    private Damageable structureInCurrentCell(Zombie self, GameSession session) {
        Cell cell = self.getCurrentCell(session);
        if (cell == null) return null;
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