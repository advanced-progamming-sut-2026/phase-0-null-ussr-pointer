package com.ussr.pvz.model.engine;

import java.util.ArrayList;
import java.util.List;

public class GameClock {
    private double elapsedSeconds;

    private final List<Tickable> entities =
            new ArrayList<>();

    public void addEntity(Tickable entity) {
        if (!entities.contains(entity)) {
            entities.add(entity);
        }
    }

    public void removeEntity(Tickable entity) {
        entities.remove(entity);
    }

    public void update(float delta) {
        if (!Float.isFinite(delta) || delta <= 0f) {
            return;
        }

        elapsedSeconds += delta;
        removeDeadEntities();

        List<Tickable> currentEntities =
                new ArrayList<>(entities);

        for (Tickable entity : currentEntities) {
            if (entity instanceof GameEntity gameEntity
                    && !gameEntity.isAlive()) {
                continue;
            }

            entity.update(delta);
        }
    }

    private void removeDeadEntities() {
        entities.removeIf(entity ->
                entity instanceof GameEntity gameEntity
                        && !gameEntity.isAlive()
        );
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void reset() {
        elapsedSeconds = 0.0;
        entities.clear();
    }
}