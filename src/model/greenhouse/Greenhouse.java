package model.greenhouse;

import java.util.List;
import java.util.Map;

public class Greenhouse {

    public static final int MAX_COLS = 5;
    public static final int MAX_ROWS = 4;
    public static final int MAX_POTS = 20;
    public static final int MARIGOLD_GROW_HOURS = 2;
    public static final int PLANT_GROW_HOURS = 8;
    public static final int MARIGOLD_COIN_REWARD = 500;

    private SproutPlant[][] grid;
    private int unlockedPots;

    public Greenhouse() {
    }

    public void unlockPot(int x, int y) {
    }

    public boolean isPotUnlocked(int x, int y) {
        return false;
    }

    public boolean isPotOccupied(int x, int y) {
        return false;
    }

    public int getUnlockedPots() {
        return unlockedPots;
    }

    public SproutPlant plant(int x, int y, List<String> unlockedPlants) {
        return null;
    }

    public SproutPlant collect(int x, int y) {
        return null;
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

    private void validateCoords(int x, int y) {
    }

    private SproutPlant randomPlant(List<String> unlockedPlants) {
        return null;
    }
}

