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
        user.setState(Plant.PlantState.ACTIVE);

        int clonesSpawned = 0;
        int maxAttempts = 50;

        while (clonesSpawned < this.cloneCount && maxAttempts > 0) {
            maxAttempts--;

            int randomX = RAND.nextInt(9) + 1;
            int randomY = RAND.nextInt(5) + 1;

            boolean cellOccupied = false;
            if (session.getPlants() != null) {
                for (Plant p : session.getPlants()) {
                    if (p.isAlive() && (int) p.getPosition().x() == randomX && (int) p.getPosition().y() == randomY) {
                        cellOccupied = true;
                        break;
                    }
                }
            }

            if (!cellOccupied) {
                Plant clone = new Plant(user);
                clone.setPosition(new Vec2(randomX, randomY));

                clone.setState(Plant.PlantState.ACTIVE);
                session.getPlants().add(clone);
                clonesSpawned++;
            }
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {

    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {

    }
}
