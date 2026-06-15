package com.ussr.pvz.model.account;

import com.ussr.pvz.model.entities.plants.Plant;

import java.util.List;

public class Collection {
    private List<Plant> unlockedPlants;
    private List<Plant> lockedPlants;

    public Collection(List<Plant> unlockedPlants, List<Plant> lockedPlants) {
        this.unlockedPlants = unlockedPlants;
        this.lockedPlants = lockedPlants;
    }

    public void unlockPlant(Plant plant){
        unlockedPlants.add(plant);
        lockedPlants.remove(plant);
    }

    public List<Plant> getLockedPlants() {
        return lockedPlants;
    }

    public List<Plant> getUnlockedPlants() {
        return unlockedPlants;
    }
}
