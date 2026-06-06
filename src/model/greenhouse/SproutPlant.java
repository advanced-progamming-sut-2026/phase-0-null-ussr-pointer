package model.greenhouse;

import java.util.Map;

public class SproutPlant {

    private String plantKey;
    private boolean isMarigold;
    private PlantState state;
    private long plantedAtMillis;
    private long growthDurationMillis;

    public SproutPlant(String plantKey, boolean isMarigold) {
    }

    public PlantState getState() {
        return state;
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
}

