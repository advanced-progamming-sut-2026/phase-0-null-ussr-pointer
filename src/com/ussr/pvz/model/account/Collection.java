package com.ussr.pvz.model.account;

import com.ussr.pvz.model.entities.plants.Plant;

import java.util.List;

/**
 * @param unlockedPlants this class may be unused in the future the unlocked plants will be known by their lvl(0) now
 */
public record Collection(List<Plant> unlockedPlants, List<Plant> lockedPlants) {

    public void unlockPlant(Plant plant) {
        unlockedPlants.add(plant);
        lockedPlants.remove(plant);
    }
}
