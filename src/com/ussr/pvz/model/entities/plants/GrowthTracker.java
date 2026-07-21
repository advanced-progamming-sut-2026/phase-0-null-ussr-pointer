package com.ussr.pvz.model.entities.plants;

import java.util.List;
import java.util.Map;

public class GrowthTracker {
    private final List<Map<String, Object>> stages;
    private int currentStage = 1;
    private double ageInSeconds = 0.0;

    public GrowthTracker(List<Map<String, Object>> stages) {
        this.stages = stages;
    }

    // Copy constructor to prevent state-sharing across cloned plant instances
    public GrowthTracker(GrowthTracker other) {
        this.stages = other.stages;
        this.currentStage = 1;
        this.ageInSeconds = 0.0;
    }

    public boolean hasStages() {
        return stages != null && !stages.isEmpty();
    }

    /**
     * Updates growth time.
     * @return true if the plant progressed to a new stage during this tick.
     */
    public boolean update(double deltaTimeSeconds) {
        if (!hasStages()) return false;

        ageInSeconds += deltaTimeSeconds;
        int previousStage = currentStage;

        for (Map<String, Object> stageData : stages) {
            if (stageData.get("stage") instanceof Number stageNum &&
                    stageData.get("time") instanceof Number timeNum) {

                int stage = stageNum.intValue();
                double targetTime = timeNum.doubleValue();

                if (ageInSeconds >= targetTime && stage > currentStage) {
                    currentStage = stage;
                }
            }
        }

        return currentStage > previousStage;
    }

    public void skipToMaxStage() {
        if (!hasStages()) return;

        int maxStage = currentStage;
        double maxTime = ageInSeconds;

        for (Map<String, Object> stageData : stages) {
            if (stageData.get("stage") instanceof Number stageNum &&
                    stageData.get("time") instanceof Number timeNum) {

                int stage = stageNum.intValue();
                double targetTime = timeNum.doubleValue();

                if (stage > maxStage) maxStage = stage;
                if (targetTime > maxTime) maxTime = targetTime;
            }
        }

        this.currentStage = maxStage;
        this.ageInSeconds = maxTime;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public double getAgeInSeconds() {
        return ageInSeconds;
    }

    public Double getStageValue(String key) {
        if (!hasStages()) return null;

        for (Map<String, Object> stageData : stages) {
            if (stageData.get("stage") instanceof Number stageNum && stageNum.intValue() == currentStage) {
                if (stageData.get(key) instanceof Number valNum) {
                    return valNum.doubleValue();
                }
            }
        }
        return null;
    }

    public List<Map<String, Object>> getRawStages() {
        return stages;
    }
}