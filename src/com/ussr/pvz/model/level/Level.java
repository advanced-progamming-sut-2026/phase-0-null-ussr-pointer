package com.ussr.pvz.model.level;

import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.level.delivery.DeliveryStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Level {

    private String id;
    private int order;
    private String chapter;
    private boolean sunFalling = true;
    private int timeLimitSeconds = 0;
    private int deadlineColumn = -1;
    private int allowedPlantsLost = -1;
    private final List<String> lockedPlants = new ArrayList<>();
    private final List<String> seedPlants = new ArrayList<>();
    private List<AllowedZombie> allowedZombies = new ArrayList<>();
    private List<Wave> waves = new ArrayList<>();

    private DeliveryStrategy deliveryStrategy;

    private LevelBehavior behavior;

    // === Ancient Egypt: sandstorm schedule (chapter effect state) ===
    private final List<SandstormEvent> sandstormSchedule = new ArrayList<>();
    private int nextSandstormIndex = 0;

    // === Big Wave Beach: tide schedule (chapter effect state) ===
    private int startingTideColumn = 9;
    private final List<TideEvent> tideSchedule = new ArrayList<>();
    private int currentTideColumn = 9;
    private int nextTideIndex = 0;

    // === Dark Ages: necromancy config (chapter effect state) ===
    private String necromancyZombieAlias;
    private int zombiesPerNecromancyWave = 0;

    // === Frostbite Caves: freezing wind config (chapter effect state) ===
    private double windIntervalSeconds = 0.0;
    private int freezeStacksPerWind = 0;
    private double windTimerElapsed = 0.0;
    private double thawTimerElapsed = 0.0;

    public void onStart() {
        if (behavior != null) behavior.onStart(this);
    }

    public void onWaveComplete(int wave) {
        if (behavior != null) behavior.onWaveComplete(this, wave);
    }

    public void onComplete() {
        if (behavior != null) behavior.onComplete(this);
    }

    // === Getters and Setters ===

    public boolean isFailed() {
        return behavior != null && behavior.isFailed(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public boolean isSunFalling() {
        return sunFalling;
    }

    public void setSunFalling(boolean sunFalling) {
        this.sunFalling = sunFalling;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(int timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public int getDeadlineColumn() {
        return deadlineColumn;
    }

    public void setDeadlineColumn(int deadlineColumn) {
        this.deadlineColumn = deadlineColumn;
    }

    public int getAllowedPlantsLost() {
        return allowedPlantsLost;
    }

    public void setAllowedPlantsLost(int allowedPlantsLost) {
        this.allowedPlantsLost = allowedPlantsLost;
    }

    public List<String> getLockedPlants() {
        return lockedPlants;
    }

    public void setLockedPlants(List<String> lockedPlants) {
        this.lockedPlants.clear();
        if (lockedPlants != null) {
            this.lockedPlants.addAll(lockedPlants);
        }
    }

    public List<String> getSeedPlants() {
        return seedPlants;
    }

    public void setSeedPlants(List<String> seedPlants) {
        this.seedPlants.clear();
        if (seedPlants != null) {
            this.seedPlants.addAll(seedPlants);
        }
    }

    public DeliveryStrategy getDeliveryStrategy() {
        return deliveryStrategy;
    }

    public void setDeliveryStrategy(DeliveryStrategy deliveryStrategy) {
        this.deliveryStrategy = deliveryStrategy;
    }

    public List<AllowedZombie> getAllowedZombies() {
        return allowedZombies;
    }

    public void setAllowedZombies(List<AllowedZombie> allowedZombies) {
        this.allowedZombies = allowedZombies != null ? allowedZombies : new ArrayList<>();
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public void setWaves(List<Wave> waves) {
        this.waves = waves != null ? waves : new ArrayList<>();
    }

    public LevelBehavior getBehavior() {
        return this.behavior;
    }

    public void setBehavior(LevelBehavior levelBehavior) {
        this.behavior = levelBehavior;
    }

    // === Ancient Egypt: sandstorm schedule ===

    public List<SandstormEvent> getSandstormSchedule() {
        return sandstormSchedule;
    }

    public void setSandstormSchedule(List<SandstormEvent> schedule) {
        this.sandstormSchedule.clear();
        if (schedule != null) {
            this.sandstormSchedule.addAll(schedule);
        }
        this.sandstormSchedule.sort(Comparator.comparingDouble(SandstormEvent::triggerTimeSeconds));
        this.nextSandstormIndex = 0;
    }

    public int getNextSandstormIndex() {
        return nextSandstormIndex;
    }

    public void setNextSandstormIndex(int nextSandstormIndex) {
        this.nextSandstormIndex = nextSandstormIndex;
    }

    // === Big Wave Beach: tide schedule ===

    public int getStartingTideColumn() {
        return startingTideColumn;
    }

    public void setStartingTideColumn(int startingTideColumn) {
        this.startingTideColumn = startingTideColumn;
        this.currentTideColumn = startingTideColumn;
    }

    public List<TideEvent> getTideSchedule() {
        return tideSchedule;
    }

    public void setTideSchedule(List<TideEvent> schedule) {
        this.tideSchedule.clear();
        if (schedule != null) {
            this.tideSchedule.addAll(schedule);
        }
        this.tideSchedule.sort(Comparator.comparingDouble(TideEvent::triggerTimeSeconds));
        this.nextTideIndex = 0;
    }

    public int getCurrentTideColumn() {
        return currentTideColumn;
    }

    public void setCurrentTideColumn(int currentTideColumn) {
        this.currentTideColumn = currentTideColumn;
    }

    public int getNextTideIndex() {
        return nextTideIndex;
    }

    public void setNextTideIndex(int nextTideIndex) {
        this.nextTideIndex = nextTideIndex;
    }

    // === Dark Ages: necromancy config ===

    public String getNecromancyZombieAlias() {
        return necromancyZombieAlias;
    }

    public void setNecromancyZombieAlias(String necromancyZombieAlias) {
        this.necromancyZombieAlias = necromancyZombieAlias;
    }

    public int getZombiesPerNecromancyWave() {
        return zombiesPerNecromancyWave;
    }

    public void setZombiesPerNecromancyWave(int zombiesPerNecromancyWave) {
        this.zombiesPerNecromancyWave = zombiesPerNecromancyWave;
    }

    // === Frostbite Caves: freezing wind config ===

    public double getWindIntervalSeconds() {
        return windIntervalSeconds;
    }

    public void setWindIntervalSeconds(double windIntervalSeconds) {
        this.windIntervalSeconds = windIntervalSeconds;
    }

    public int getFreezeStacksPerWind() {
        return freezeStacksPerWind;
    }

    public void setFreezeStacksPerWind(int freezeStacksPerWind) {
        this.freezeStacksPerWind = freezeStacksPerWind;
    }

    public double getWindTimerElapsed() {
        return windTimerElapsed;
    }

    public void setWindTimerElapsed(double windTimerElapsed) {
        this.windTimerElapsed = windTimerElapsed;
    }

    public double getThawTimerElapsed() {
        return thawTimerElapsed;
    }

    public void setThawTimerElapsed(double thawTimerElapsed) {
        this.thawTimerElapsed = thawTimerElapsed;
    }

    // === Core Inner Records ===

    public record AllowedZombie(String id, int weight) {}

    public record Wave(int waveNumber, int cost, List<SpawnData> spawnData) {}

    public record SpawnData(String zombieId, int lane, float delaySeconds) {}

    public record SandstormEvent(double triggerTimeSeconds, String zombieAlias) {}

    public record TideEvent(double triggerTimeSeconds, int targetColumn) {}
}