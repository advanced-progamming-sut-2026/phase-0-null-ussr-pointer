package com.ussr.pvz.model.greenhouse;

import com.ussr.pvz.model.account.Collection;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodType;

import java.security.SecureRandom;
import java.util.*;

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
        if(pots.get(new Location(x, y)).getPlant().isReady()) {
            throw new IllegalStateException("SproutPlant is ready");
        }

        return pots.get(new Location(x,y)).getPlant().getRemainingHoursCeil();
    }

    public void grow(int x, int y) {
        if(pots.get(new Location(x, y)).getPlant().isReady()) {
            throw new IllegalStateException("SproutPlant is ready");
        }

        pots.get(new Location(x, y)).getPlant().setState(PlantState.READY);
    }

    public void printGrid() {
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("unlockedPots", unlockedPots);

        List<Map<String, Object>> potsList = new ArrayList<>();
        for (Map.Entry<Location, Pot> entry : pots.entrySet()) {
            Pot pot = entry.getValue();
            Map<String, Object> potMap = new HashMap<>();
            potMap.put("x", pot.getX());
            potMap.put("y", pot.getY());
            potMap.put("unlocked", pot.isUnlocked());
            potMap.put("occupied", pot.isOccupied());
            if (pot.getPlant() != null) {
                potMap.put("plant", pot.getPlant().toMap());
            }
            potsList.add(potMap);
        }
        map.put("pots", potsList);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Greenhouse fromMap(Map<String, Object> map) {
        if (map == null) return new Greenhouse();

        Greenhouse gh = new Greenhouse();
        gh.unlockedPots = ((Number) map.get("unlockedPots")).intValue();

        List<Map<String, Object>> potsList = (List<Map<String, Object>>) map.get("pots");
        if (potsList != null) {
            for (Map<String, Object> potMap : potsList) {
                int x = ((Number) potMap.get("x")).intValue();
                int y = ((Number) potMap.get("y")).intValue();
                boolean unlocked = (boolean) potMap.get("unlocked");
                boolean occupied = (boolean) potMap.get("occupied");

                Pot pot = gh.pots.get(new Location(x, y));
                pot.setUnlocked(unlocked);
                pot.setOccupied(occupied);

                if (potMap.containsKey("plant")) {
                    pot.setPlant(SproutPlant.fromMap((Map<String, Object>) potMap.get("plant")));
                }
            }
        }
        return gh;

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
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Location l)) return false;
            return x == l.x && y == l.y;
        }
    }
}

