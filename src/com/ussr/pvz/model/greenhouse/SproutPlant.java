package com.ussr.pvz.model.greenhouse;

import java.util.Map;
import java.util.HashMap;

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
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - plantedAtMillis;
        long remaining = growthDurationMillis - elapsedTime;
        return Math.max(0, remaining);
    }

    public int getRemainingHoursCeil() {
        long remainingMillis = getRemainingMillis();
        long remainingHours = (remainingMillis + 3599999) / 3600000; // Ceiling division
        return (int) remainingHours;
    }

    public boolean isReady() {
        return getRemainingMillis() == 0 || state == PlantState.READY;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("plantKey", plantKey);
        map.put("isMarigold", isMarigold);
        map.put("state", state.name());
        map.put("type", type);
        map.put("plantedAtMillis", plantedAtMillis);
        map.put("growthDurationMillis", growthDurationMillis);
        return map;
    }

    public static SproutPlant fromMap(Map<String, Object> map) {
        String plantKey = (String) map.get("plantKey");

        // Safer boolean parsing to avoid null crashes
        boolean isMarigold = Boolean.TRUE.equals(map.get("isMarigold"));

        PlantState state = PlantState.valueOf((String) map.get("state"));
        String type = (String) map.get("type");

        // 👇 The Fix: Cast to Number first, then extract the long value 👇
        long plantedAtMillis = ((Number) map.get("plantedAtMillis")).longValue();
        long growthDurationMillis = ((Number) map.get("growthDurationMillis")).longValue();

        return new SproutPlant(plantKey, isMarigold, state, type, plantedAtMillis, growthDurationMillis);
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