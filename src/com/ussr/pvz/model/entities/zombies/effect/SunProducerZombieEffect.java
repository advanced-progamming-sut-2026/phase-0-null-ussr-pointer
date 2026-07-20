package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class SunProducerZombieEffect implements EffectStatus {
    private double currentInterval = 20.0; // Starts slow
    private final double minInterval = 5.0; // Caps at 5 seconds
    private final double intervalDecrement = 1.0; // Speeds up per drop
    private double timer = currentInterval;

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) return;

        timer -= GameClock.SECONDS_PER_TICK;
        if (timer <= 0) {
            int zRow = (int) zombie.getPosition().y();
            int zCol = (int) zombie.getPosition().x();
            ProducedSun sun = new ProducedSun(zCol, zRow, 50, zombie.getAlias());
            session.addItem(sun);

            if (currentInterval > minInterval) {
                currentInterval -= intervalDecrement;
                if (currentInterval < minInterval) currentInterval = minInterval;
            }
            timer = currentInterval;
        }
    }
}