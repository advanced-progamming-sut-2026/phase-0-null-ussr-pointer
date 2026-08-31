package com.ussr.pvz.model.engine.session;

public final class SessionResources {
    private int sun;
    private int plantFood;

    public int getSun() {
        return sun;
    }

    public void addSun(int amount) {
        sun += amount;
    }

    public boolean spendSun(int amount) {
        if (amount < 0 || sun < amount) {
            return false;
        }

        sun -= amount;
        return true;
    }

    public int getPlantFood() {
        return plantFood;
    }

    public void addPlantFood() {
        plantFood = Math.min(plantFood + 1, 3);
    }

    public boolean spendPlantFood() {
        if (plantFood <= 0) {
            return false;
        }

        plantFood--;
        return true;
    }
}