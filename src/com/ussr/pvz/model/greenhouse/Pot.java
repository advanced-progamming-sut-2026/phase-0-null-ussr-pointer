package com.ussr.pvz.model.greenhouse;

public class Pot {
    private SproutPlant plant;
    private final int x;
    private final int y;
    private boolean isUnlocked;
    private boolean isOccupied;

    public Pot(int x, int y) {
        this.x = x;
        this.y = y;
        isUnlocked = false;
        isOccupied = false;
    }

    public SproutPlant getPlant() {
        return plant;
    }

    public void setPlant(SproutPlant plant) {
        this.plant = plant;
        isUnlocked = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }
}
