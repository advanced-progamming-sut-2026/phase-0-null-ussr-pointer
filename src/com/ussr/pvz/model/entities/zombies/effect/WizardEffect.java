package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.board.Cell;

import java.util.ArrayList;
import java.util.List;

public class WizardEffect implements EffectStatus {
    private final double transformInterval;
    private double timer = 0.0;

    private final List<Plant> cursedPlants = new ArrayList<>();
    private boolean deathHandled = false;

    public WizardEffect(double transformInterval) {
        this.transformInterval = transformInterval;
    }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) {
            if (!deathHandled) {
                for (Plant cursedPlant : cursedPlants) {
                    if (cursedPlant.isAlive()) {
                        cursedPlant.setCat(false);
                    }
                }
                cursedPlants.clear();
                deathHandled = true;
            }
            return;
        }

        timer += GameClock.SECONDS_PER_TICK;
        if (timer >= transformInterval) {
            timer = 0;
            transformRandomPlant(session);
        }

        handleCollisionTransformation(zombie, session);
    }

    private void transformRandomPlant(GameSession session) {
        List<Plant> validPlants = new ArrayList<>();
        for (Plant p : session.getPlants()) {
            if (p.isAlive() && !p.isCat()) {
                validPlants.add(p);
            }
        }

        if (!validPlants.isEmpty()) {
            int randomIndex = (int) (Math.random() * validPlants.size());
            Plant target = validPlants.get(randomIndex);
            applyCurse(target);
        }
    }

    private void handleCollisionTransformation(Zombie zombie, GameSession session) {
        int zRow = (int) zombie.getPosition().y();
        double zCol = zombie.getPosition().x();
        int checkCol = (int) Math.floor(zCol);

        if (checkCol >= 0 && checkCol < session.getLawn().getCols()) {
            Cell cell = session.getLawn().getCell(zRow, checkCol);

            if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive() && !cell.getPlant().isCat()) {
                applyCurse(cell.getPlant());
            }
        }
    }

    private void applyCurse(Plant target) {
        target.setCat(true);
        cursedPlants.add(target);
    }
}