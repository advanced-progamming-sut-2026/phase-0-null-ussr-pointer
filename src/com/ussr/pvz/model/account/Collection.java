package com.ussr.pvz.model.account;

import com.ussr.pvz.model.entities.plants.Plant;

import java.util.List;

public class Collection {
    //this class may be unused in the future the unlocked plants will be known by their lvl(0) now
    private final List<Plant> unlockedPlants;
    private final List<Plant> lockedPlants;

    public Collection(List<Plant> unlockedPlants, List<Plant> lockedPlants) {
        this.unlockedPlants = unlockedPlants;
        this.lockedPlants = lockedPlants;
    }

    public void unlockPlant(Plant plant) {
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
