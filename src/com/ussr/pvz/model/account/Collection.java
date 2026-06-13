package com.ussr.pvz.model.account;

import com.ussr.pvz.model.entities.plants.BasePlant;

import java.util.List;

public class Collection {
    private List<BasePlant> unlockedPlants;
    private List<BasePlant> lockedPlants;

    public Collection(List<BasePlant> unlockedPlants, List<BasePlant> lockedPlants) {
        this.unlockedPlants = unlockedPlants;
        this.lockedPlants = lockedPlants;
    }

    public void unlockPlant(BasePlant plant){
        unlockedPlants.add(plant);
        lockedPlants.remove(plant);
    }

    public List<BasePlant> getLockedPlants() {
        return lockedPlants;
    }

    public List<BasePlant> getUnlockedPlants() {
        return unlockedPlants;
    }
}
