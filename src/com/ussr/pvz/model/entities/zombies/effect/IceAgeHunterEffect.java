package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.projectiles.SnowballProjectile;
import com.ussr.pvz.model.util.Vec2;

public class IceAgeHunterEffect implements EffectStatus {

    private final double throwCooldown;
    private double timer;

    public IceAgeHunterEffect(double throwCooldown) {
        this.throwCooldown = throwCooldown;
        this.timer = throwCooldown; // Start ready to throw
    }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) return;

        timer += GameClock.SECONDS_PER_TICK;

        if (timer >= throwCooldown) {
            boolean thrown = throwSnowball(zombie, session);
            if (thrown) {
                timer = 0;
            }
        }
    }

    private boolean throwSnowball(Zombie zombie, GameSession session) {
        int zRow = (int) zombie.getPosition().y();
        double zCol = zombie.getPosition().x();
        int cols = session.getLawn().getCols();

        // Scan the row to find a valid plant
        for (int c = (int) zCol; c >= 0; c--) {
            if (c >= cols) continue;

            Cell cell = session.getLawn().getCell(zRow, c);
            if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
                Plant targetPlant = cell.getPlant();

                Vec2 startPos = zombie.getPosition();
                Vec2 targetPos = Vec2.of(targetPlant.getLocation().x(), targetPlant.getLocation().y());

                // Throw the snowball (flies faster than an octopus, e.g., 0.8 seconds)
                SnowballProjectile snowball = new SnowballProjectile(startPos, targetPos, 0.8);
                session.addZombieProjectile(snowball);

                return true;
            }
        }
        return false;
    }
}