package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PullUnderWater implements PlantFoodEffect {
    private final int zombieCount;

    public PullUnderWater(int zombieCount) {
        this.zombieCount = zombieCount;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (session.getZombies() == null || session.getZombies().isEmpty()) return;

        List<Zombie> activeZombies = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive()) {
                if(session.getLawn().getCell((int) zombie.getPosition().y() ,
                        (int) zombie.getPosition().x()).getTile().getType() == TileType.Water)
                    activeZombies.add(zombie);
            }
        }

        Collections.shuffle(activeZombies);
        int targetsToPull = Math.min(this.zombieCount, activeZombies.size());

        for (int i = 0; i < targetsToPull; i++) {
            activeZombies.get(i).takeDamage(9999, user);
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {

    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {

    }
}
