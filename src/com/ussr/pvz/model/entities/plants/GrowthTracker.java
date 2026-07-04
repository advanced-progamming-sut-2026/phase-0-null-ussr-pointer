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

    public boolean hasStages() {
        return stages != null && !stages.isEmpty();
    }

    public void update(double deltaTimeSeconds) {
        if (!hasStages()) return;
        ageInSeconds += deltaTimeSeconds;
        for (Map<String, Object> stageData : stages) {
            int stage = ((Double) stageData.get("stage")).intValue();
            double targetTime = (Double) stageData.get("time");
            if (ageInSeconds >= targetTime && stage > currentStage) {
                currentStage = stage;
            }
        }
    }

    public void skipToMaxStage() {
        if (!hasStages()) return;

        int maxStage = currentStage;
        double maxTime = ageInSeconds;

        for (Map<String, Object> stageData : stages) {
            int stage = ((Double) stageData.get("stage")).intValue();
            double targetTime = (Double) stageData.get("time");

            if (stage > maxStage) {
                maxStage = stage;
            }
            if (targetTime > maxTime) {
                maxTime = targetTime;
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
            int stage = ((Double) stageData.get("stage")).intValue();
            if (stage == currentStage && stageData.containsKey(key)) {
                return (Double) stageData.get(key);
            }
        }
        return null;
    }

    public List<Map<String, Object>> getRawStages() {
        return stages;
    }
}