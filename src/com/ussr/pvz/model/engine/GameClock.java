package com.ussr.pvz.model.engine;

import java.util.ArrayList;
import java.util.List;

public class GameClock {
    public static final double SECONDS_PER_TICK = 0.1;

    private int tick = 0;
    private final List<Tickable> entities = new ArrayList<>();

    public void addEntity(Tickable entity) {
        if (!entities.contains(entity)) {
            entities.add(entity);
        }
    }

    public void removeEntity(Tickable entity) {
        entities.remove(entity);
    }

    public void tick() {
        tick++;

        // 1. Remove dead entities to prevent memory leaks and ghost ticks
        entities.removeIf(e -> {
            if (e instanceof GameEntity ge) {
                return !ge.isAlive();
            }
            return false;
        });

        // 2. Iterate over a copy of the list to prevent ConcurrentModificationException
        // when new entities (like projectiles or dropped loot) are spawned during a tick.
        List<Tickable> currentEntities = new ArrayList<>(entities);
        for (Tickable entity : currentEntities) {
            // Only tick if it didn't die earlier in this exact same tick loop
            if (entity instanceof GameEntity ge && !ge.isAlive()) {
                continue;
            }
            entity.tick();
        }
    }

    public int getTicks() {
        return tick;
    }

    public double getElapsedSeconds() {
        return tick * SECONDS_PER_TICK;
    }

    public void reset() {
        tick = 0;
        entities.clear();
    }
}