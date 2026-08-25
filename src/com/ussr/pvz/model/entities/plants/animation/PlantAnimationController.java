package com.ussr.pvz.model.entities.plants.animation;

public class PlantAnimationController {

    private PlantAnimationState state =
            PlantAnimationState.IDLE;

    private String currentClip = "idle";
    private float remainingDuration;

    public void update(float delta) {
        if (remainingDuration <= 0f) {
            return;
        }

        remainingDuration -= delta;

        if (remainingDuration <= 0f) {
            playIdle();
        }
    }

    public void playIdle() {
        state = PlantAnimationState.IDLE;
        currentClip = "idle";
        remainingDuration = 0f;
    }

    public void playAttack(
            String plantName,
            int growthStage,
            float duration
    ) {
        playTemporary(
                PlantAnimationState.ATTACKING,
                attackClip(plantName, growthStage),
                duration
        );
    }

    public void playGrow(
            String plantName,
            int growthStage,
            float duration
    ) {
        playTemporary(
                PlantAnimationState.GROWING,
                growClip(plantName, growthStage),
                duration
        );
    }

    private String growClip(String plantName, int growthStage) {
        if (plantName != null && plantName.equalsIgnoreCase("Kiwibeast")) {
            int stage = Math.max(1, Math.min(3, growthStage));
            return "growth_stage" + stage;
        }
        return "grow";
    }

    public void playProduce(String plantName, float duration) {
        playTemporary(
                PlantAnimationState.PRODUCING,
                "Gold Bloom".equalsIgnoreCase(plantName)
                        ? "attack"
                        : "special",
                duration
        );
    }

    private String attackClip(String plantName, int growthStage) {
        if (plantName == null) {
            return "attack";
        }

        if (plantName.equalsIgnoreCase("Puff-shroom")) {
            int stage = Math.max(1, Math.min(3, growthStage));
            return "special_stage" + stage;
        }

        if (plantName.equalsIgnoreCase("Bowling Bulb")
                || plantName.equalsIgnoreCase("Fume-shroom")
                || plantName.equalsIgnoreCase("Magnet-shroom")) {
            return "special";
        }

        if (plantName.equalsIgnoreCase("Chomper")) {
            return "swallow";
        }

        if (plantName.equalsIgnoreCase("Kiwibeast")) {
            int stage = Math.max(1, Math.min(3, growthStage));
            return "attack_stage" + stage;
        }

        return "attack";
    }

    public void playPreparing() {
        playLoop(
                PlantAnimationState.PREPARING,
                "prepping"
        );
    }

    public void playPlantFood() {
        playLoop(
                PlantAnimationState.PLANT_FOOD,
                "plantfood"
        );
    }

    public void playIncapacitated() {
        playLoop(
                PlantAnimationState.INCAPACITATED,
                "idle"
        );
    }

    public void playDying(float duration) {
        playTemporary(
                PlantAnimationState.DYING,
                "attack",
                duration
        );
    }

    private void playTemporary(
            PlantAnimationState newState,
            String clip,
            float duration
    ) {
        state = newState;
        currentClip = clip;
        remainingDuration = Math.max(0f, duration);
    }

    private void playLoop(
            PlantAnimationState newState,
            String clip
    ) {
        state = newState;
        currentClip = clip;
        remainingDuration = 0f;
    }

    public PlantAnimationState getState() {
        return state;
    }

    public String getCurrentClip() {
        return currentClip;
    }
}