package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class SunProducerZombieEffect implements EffectStatus {
    private double currentInterval = 10.0; // Starts slow (10 seconds)
    private final double minInterval = 2.0; // Caps at 2 seconds
    private final double intervalDecrement = 0.5; // Speeds up by 0.5s every drop
    private double timer = currentInterval;

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) return;

        timer -= GameClock.SECONDS_PER_TICK;
        if (timer <= 0) {
            // Drop sun at the zombie's location
            int zRow = (int) zombie.getPosition().y();
            int zCol = (int) zombie.getPosition().x();
            ProducedSun sun = new ProducedSun(zCol, zRow, 50);
            session.addItem(sun);

            // Ramp up production rate based on custom formula requirement
            if (currentInterval > minInterval) {
                currentInterval -= intervalDecrement;
                if (currentInterval < minInterval) currentInterval = minInterval;
            }
            timer = currentInterval;
        }
    }
}