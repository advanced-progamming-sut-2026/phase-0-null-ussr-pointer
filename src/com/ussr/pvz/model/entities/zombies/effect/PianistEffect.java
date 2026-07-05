package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class PianistEffect implements EffectStatus {

    private final double danceInterval;
    private double timer = 0.0;

    public PianistEffect(double danceInterval) {
        this.danceInterval = danceInterval;
    }

    @Override
    public void effect(Zombie pianist, GameSession session) {
        if (!pianist.isAlive()) return;

        timer += GameClock.SECONDS_PER_TICK;

        if (timer >= danceInterval) {
            timer = 0;
            makeZombiesDance(pianist, session);
        }
    }

    private void makeZombiesDance(Zombie pianist, GameSession session) {
        int maxRows = session.getLawn().getRows();

        for (Zombie z : session.getZombies()) {
            if (!z.isAlive() || z == pianist || z.getFaction() != pianist.getFaction()) {
                continue;
            }

            int currentRow = (int) z.getPosition().y();

            int direction = Math.random() < 0.5 ? -1 : 1;
            int newRow = currentRow + direction;

            if (newRow >= 0 && newRow < maxRows) {
                z.setPosition(Vec2.of(z.getPosition().x(), newRow));
            } else {

                newRow = currentRow - direction;
                if (newRow >= 0 && newRow < maxRows) {
                    z.setPosition(Vec2.of(z.getPosition().x(), newRow));
                }
            }
        }
    }
}