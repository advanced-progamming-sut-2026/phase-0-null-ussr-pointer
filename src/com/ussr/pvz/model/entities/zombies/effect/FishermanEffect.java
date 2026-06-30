package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class FishermanEffect implements EffectStatus {

    private final double delayBetweenCasting;
    private double timer;

    public FishermanEffect(double delayBetweenCasting) {
        this.delayBetweenCasting = delayBetweenCasting;
        this.timer = delayBetweenCasting;
    }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) return;

        int rightmostCol = session.getLawn().getCols() - 1;
        if (zombie.getPosition().x() < rightmostCol) {
            zombie.setPosition(Vec2.of(rightmostCol, zombie.getPosition().y()));
        }

        timer += GameClock.SECONDS_PER_TICK;

        if (timer >= delayBetweenCasting) {
            boolean hooked = castHook(zombie, session);
            if (hooked) {
                timer = 0;
            }
        }
    }

    private boolean castHook(Zombie zombie, GameSession session) {
        int zRow = (int) zombie.getPosition().y();
        int zCol = (int) zombie.getPosition().x();

        for (int c = zCol - 1; c >= 0; c--) {
            Cell currentCell = session.getLawn().getCell(zRow, c);

            if (currentCell != null && currentCell.getPlant() != null && currentCell.getPlant().isAlive()) {
                Plant hookedPlant = currentCell.getPlant();
                int currentX = hookedPlant.getLocation().x();
                int targetX = currentX + 1;

                if (targetX >= zCol) {
                    hookedPlant.takeDamage(hookedPlant.getHp());
                    return true;
                }

                Cell targetCell = session.getLawn().getCell(zRow, targetX);
                if (targetCell != null && targetCell.getPlant() == null && targetCell.getInteractableStructure() == null) {

                    currentCell.setPlant(null);

                    targetCell.setPlant(hookedPlant);

                    hookedPlant.setLocation(new Plant.Location(targetX, zRow));
                    hookedPlant.setPosition(Vec2.of(targetX, zRow));

                    return true;
                }

                return false;
            }
        }
        return false;
    }
}