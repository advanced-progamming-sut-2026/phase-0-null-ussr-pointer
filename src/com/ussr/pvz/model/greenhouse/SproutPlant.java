package com.ussr.pvz.model.greenhouse;

import com.ussr.pvz.model.entities.plants.PlantType;

import java.util.Map;

public class SproutPlant {

    private final String plantKey;
    private final boolean isMarigold;
    private PlantState state;
    private String type;
    private long plantedAtMillis;
    private long growthDurationMillis;

    public SproutPlant(String plantKey, boolean isMarigold, PlantState state, String type, long plantedAtMillis, long growthDurationMillis) {
        this.plantKey = plantKey;
        this.isMarigold = isMarigold;
        this.state = state;
        this.type = type;
        this.plantedAtMillis = plantedAtMillis;
        this.growthDurationMillis = growthDurationMillis;
    }

    public PlantState getState() {
        return state;
    }

    public void setState(PlantState state) {
        this.state = state;
    }

    public long getRemainingMillis() {
        return 0;
    }

    public int getRemainingHoursCeil() {
        return 0;
    }

    public boolean isReady() {
        return getState() == PlantState.READY;
    }

    // serialization
    public Map<String, Object> toMap() {
        return null;
    }

    public static SproutPlant fromMap(Map<String, Object> map) {
        return null;
    }

    public long getPlantedAtMillis() {
        return plantedAtMillis;
    }

    public void setPlantedAtMillis(long plantedAtMillis) {
        this.plantedAtMillis = plantedAtMillis;
    }

    public long getGrowthDurationMillis() {
        return growthDurationMillis;
    }

    public void setGrowthDurationMillis(long growthDurationMillis) {
        this.growthDurationMillis = growthDurationMillis;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMarigold() {
        return isMarigold;
    }

    public String getPlantKey() {
        return plantKey;
    }
}

