package com.ussr.pvz.model.greenhouse;

import com.ussr.pvz.model.account.Collection;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodType;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Greenhouse {

    public static final int MAX_COLS = 5;
    public static final int MAX_ROWS = 4;
    private static final long SECOND = 1000L;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    public static final int MAX_POTS = 20;
    public static final int MARIGOLD_GROW_HOURS = 2;
    public static final int PLANT_GROW_HOURS = 8;
    public static final int MARIGOLD_COIN_REWARD = 500;
    private final HashMap<Location, Pot> pots;

    private SproutPlant[][] grid;
    private int unlockedPots;

    public Greenhouse() {
        pots = new HashMap<>();
        for (int i = 0; i < MAX_COLS; i++) {
            for (int j = 0; j < MAX_ROWS; j++) {
                Location l = new Location(i, j);
                pots.put(l, new Pot(l.getX(), l.getY()));
            }
        }
    }

    public void unlockPot(int x, int y) {
        pots.get(new Location(x, y)).setUnlocked(true);
        unlockedPots++;
    }

    public boolean isPotUnlocked(int x, int y) {
        return pots.get(new Location(x, y)).isUnlocked();
    }

    public boolean isPotOccupied(int x, int y) {
        return pots.get(new Location(x, y)).isOccupied();
    }

    public int getUnlockedPots() {
        return unlockedPots;
    }

    public void plant(int x, int y, Collection collection) {
        Random rand = new SecureRandom();
        rand.setSeed(System.currentTimeMillis());
        SproutPlant plant = null;
        if (rand.nextInt() % 2 == 0) {
            plant = randomPlant(collection);
        } else {
            plant = new SproutPlant(null, true, PlantState.GROWING, "",System.currentTimeMillis(), 2 * HOUR);
        }

        pots.get(new Location(x, y)).setPlant(plant);
    }

    public SproutPlant collect(int x, int y) {
        SproutPlant result = pots.get(new Location(x, y)).getPlant();
        if(result.isReady()) {
            pots.get(new Location(x, y)).setOccupied(false);
            return result;
        }
        else
            throw new IllegalStateException("SproutPlant not ready");
    }

    public int speedUp(int x, int y) {
        return 0;
    }

    public void printGrid() {
    }

    public Map<String, Object> toMap() {
        return null;
    }

    public static Greenhouse fromMap(Map<String, Object> map) {
        return null;
    }

    private SproutPlant randomPlant(Collection collection) {
        SecureRandom random = new SecureRandom();
        int min = 0;
        int max = collection.getUnlockedPlants().size();
        int randomInt = random.nextInt(max - min + 1) + min;
        Plant typePlant = collection.getUnlockedPlants().get(randomInt);
        while (typePlant.getPlantFoodType().equals(PlantFoodType.NONE)) {
            typePlant = collection.getUnlockedPlants().get(randomInt++);
        }
        return new SproutPlant(null, false, PlantState.GROWING, typePlant.getName(), System.currentTimeMillis(), 8 * HOUR);
    }

    private static class Location {
        int x;
        int y;

        private Location(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}

