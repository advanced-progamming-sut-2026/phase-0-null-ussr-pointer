package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class JalapenoZombieEffect implements EffectStatus {
    private double timer = 0.0;
    private final double fuseTime = 10.0;

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) return;

        timer += GameClock.SECONDS_PER_TICK;

        if (timer >= fuseTime) {
            int row = (int) zombie.getPosition().y();
            int cols = session.getLawn().getCols();

            // Annihilate the entire row
            for (int c = 0; c < cols; c++) {
                Cell cell = session.getLawn().getCell(row, c);
                if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
                    // Send out the incineration event for CLI/Logging
                    session.getEventBus().publish(new GameEvent.PlantIncinerated(
                            cell.getPlant().getName(),
                            zombie.getAlias(),
                            row,
                            c
                    ));

                    cell.getPlant().takeDamage(99999, zombie);
                }
            }

            zombie.takeDamage(zombie.getHp(), false);
        }
    }
}