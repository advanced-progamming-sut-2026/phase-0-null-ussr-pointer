package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.projectiles.OctopusProjectile;
import com.ussr.pvz.model.util.Vec2;

public class OctopusThrowEffect implements EffectStatus {

    private final double throwCooldown;
    private double timer;

    public OctopusThrowEffect(double throwCooldown) {
        this.throwCooldown = throwCooldown;
        this.timer = throwCooldown;
    }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) return;

        timer += GameClock.SECONDS_PER_TICK;

        if (timer >= throwCooldown) {
            boolean thrown = throwOctopus(zombie, session);
            if (thrown) {
                timer = 0;
            }
        }
    }

    private boolean throwOctopus(Zombie zombie, GameSession session) {
        int zRow = (int) zombie.getPosition().y();
        double zCol = zombie.getPosition().x();
        int cols = session.getLawn().getCols();

        for (int c = (int) zCol; c >= 0; c--) {
            if (c >= cols) continue;

            Cell cell = session.getLawn().getCell(zRow, c);
            if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
                Plant targetPlant = cell.getPlant();

                // TODO: You will need to check if the plant ALREADY has an octopus on it so he doesn't double-throw.
                // e.g., if (!targetPlant.isOctopused())

                Vec2 startPos = zombie.getPosition();
                Vec2 targetPos = Vec2.of(targetPlant.getLocation().x(), targetPlant.getLocation().y());

                OctopusProjectile octopus = new OctopusProjectile(startPos, targetPos, 1.5);
                session.addZombieProjectile(octopus);

                return true;
            }
        }
        return false;
    }
}