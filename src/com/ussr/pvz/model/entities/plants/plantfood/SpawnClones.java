package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.util.Vec2;

import java.util.Random;

public class SpawnClones implements PlantFoodEffect {
    private final int cloneCount;
    private static final Random RAND = new Random();

    public SpawnClones(int cloneCount) {
        this.cloneCount = cloneCount;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (user == null || session == null) return;

        armPlant(user);

        int clonesSpawned = 0;
        int maxAttempts = 50;

        // 2. Find random empty cells and spawn armed clones
        while (clonesSpawned < this.cloneCount && maxAttempts > 0) {
            maxAttempts--;

            int randomX = RAND.nextInt(9);
            int randomY = RAND.nextInt(5);

            boolean cellOccupied = false;
            if (session.getPlants() != null) {
                for (Plant p : session.getPlants()) {
                    if (p != null && p.isAlive()) {
                        Plant.Location loc = p.getLocation();
                        if (loc != null && loc.x() == randomX && loc.y() == randomY) {
                            cellOccupied = true;
                            break;
                        }
                    }
                }
            }

            if (!cellOccupied) {
                Plant clone = new Plant(user);
                clone.setPosition(Vec2.of(randomX, randomY));

                armPlant(clone);

                session.getPlants().add(clone);
                clonesSpawned++;
            }
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        // Instant superpower trigger; no continuous stat modifiers
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        // Instant superpower trigger; no continuous tick handling needed
    }

    private void armPlant(Plant plant) {
        plant.setState(Plant.PlantState.ACTIVE);
        plant.setInternalTimer(0.0);
        //todo : if you got a crash first check next line (may be comment it)
        plant.instantlyMature(); // Skips growth/arming timers if using GrowthTracker
    }
}