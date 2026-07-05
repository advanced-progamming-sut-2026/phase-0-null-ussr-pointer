package com.ussr.pvz.model.greenhouse;

import com.ussr.pvz.model.account.Collection;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodType;

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

    private static final Random RAND = new Random();

    private final HashMap<Location, Pot> pots;
    private int unlockedPots;

    public Greenhouse() {
        pots = new HashMap<>();
        for (int i = 0; i < MAX_COLS; i++) {
            for (int j = 0; j < MAX_ROWS; j++) {
                Location l = new Location(i, j);
                pots.put(l, new Pot(l.x(), l.y()));
            }
        }
    }

    public void unlockPot(int x, int y) {
        Pot pot = pots.get(new Location(x, y));
        if (pot != null && !pot.isUnlocked()) {
            pot.setUnlocked(true);
            unlockedPots++;
        }
    }

    public void unlockPot() {
        for (int j = 0; j < MAX_ROWS; j++) {
            for (int i = 0; i < MAX_COLS; i++) {
                Location loc = new Location(i, j);
                Pot pot = pots.get(loc);
                if (pot != null && !pot.isUnlocked()) {
                    pot.setUnlocked(true);
                    unlockedPots++;
                    return;
                }
            }
        }
    }

    public boolean isPotUnlocked(int x, int y) {
        Pot pot = pots.get(new Location(x, y));
        return pot != null && pot.isUnlocked();
    }

    public boolean isPotOccupied(int x, int y) {
        Pot pot = pots.get(new Location(x, y));
        return pot != null && pot.isOccupied();
    }

    public int getUnlockedPots() {
        return unlockedPots;
    }

    public void plant(int x, int y, Collection collection) {
        Pot targetPot = pots.get(new Location(x, y));
        if (targetPot == null || !targetPot.isUnlocked() || targetPot.isOccupied()) {
            return;
        }

        SproutPlant plant;
        if (RAND.nextInt(2) == 0) {
            plant = randomPlant(collection);
        } else {
            plant = new SproutPlant("MARIGOLD", true, PlantState.GROWING, "MARIGOLD", System.currentTimeMillis(), 2 * HOUR);
        }

        targetPot.setPlant(plant);
        targetPot.setOccupied(true);
    }

    public SproutPlant collect(int x, int y) {
        Pot pot = pots.get(new Location(x, y));
        if (pot == null) {
            throw new IllegalArgumentException("Invalid location");
        }

        SproutPlant result = pot.getPlant();
        if (result == null) {
            throw new IllegalStateException("No plant present");
        }

        if (result.isReady()) {
            pot.setPlant(null);
            pot.setOccupied(false);
            return result;
        } else {
            throw new IllegalStateException("SproutPlant not ready");
        }
    }

    public int speedUp(int x, int y) {
        Pot pot = pots.get(new Location(x, y));
        if (pot == null || pot.getPlant() == null) {
            throw new IllegalStateException("No plant present");
        }
        if (pot.getPlant().isReady()) {
            throw new IllegalStateException("SproutPlant is ready");
        }
        return pot.getPlant().getRemainingHoursCeil();
    }

    public void grow(int x, int y) {
        Pot pot = pots.get(new Location(x, y));
        if (pot == null || pot.getPlant() == null) {
            throw new IllegalStateException("No plant present");
        }
        if (pot.getPlant().isReady()) {
            throw new IllegalStateException("SproutPlant is ready");
        }
        pot.getPlant().setState(PlantState.READY);
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
                if (pot != null) {
                    pot.setUnlocked(unlocked);
                    pot.setOccupied(occupied);
                    if (potMap.containsKey("plant")) {
                        pot.setPlant(SproutPlant.fromMap((Map<String, Object>) potMap.get("plant")));
                    }
                }
            }
        }
        return gh;
    }

    private SproutPlant randomPlant(Collection collection) {
        List<Plant> unlocked = collection.unlockedPlants();
        if (unlocked == null || unlocked.isEmpty()) {
            return new SproutPlant("MARIGOLD", true, PlantState.GROWING, "MARIGOLD", System.currentTimeMillis(), 2 * HOUR);
        }

        int max = unlocked.size();
        int initialIdx = RAND.nextInt(max);
        int currentIdx = initialIdx;
        Plant typePlant = unlocked.get(currentIdx);

        while (typePlant.getPlantFoodType().equals(PlantFoodType.NONE)) {
            currentIdx = (currentIdx + 1) % max;
            typePlant = unlocked.get(currentIdx);
            if (currentIdx == initialIdx) {
                break;
            }
        }

        return new SproutPlant(typePlant.getName(), false, PlantState.GROWING, typePlant.getName(), System.currentTimeMillis(), 8 * HOUR);
    }

    private record Location(int x, int y) {

        @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Location(int x1, int y1))) return false;
                return x == x1 && y == y1;
            }
    }
}