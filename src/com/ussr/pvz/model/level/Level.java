package com.ussr.pvz.model.level;

import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.level.delivery.DeliveryStrategy;
import com.ussr.pvz.model.level.environment.Environment;

import java.util.ArrayList;
import java.util.List;

public class Level {

    private String id;
    private int order;
    private String chapter;
    private Environment environment;
    private boolean sunFalling = true;
    private int timeLimitSeconds = 0;
    private int deadlineColumn = -1;
    private int allowedPlantsLost = -1;
    private List<String> lockedPlants = new ArrayList<>();
    private List<String> seedPlants = new ArrayList<>();

    private DeliveryStrategy deliveryStrategy;

    private List<AllowedZombie> allowedZombies;
    private List<Wave> waves = new ArrayList<>();

    private LevelBehavior behavior;

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
        this.lockedPlants = lockedPlants;
    }

    public List<String> getSeedPlants() {
        return seedPlants;
    }

    public void setSeedPlants(List<String> seedPlants) {
        this.seedPlants = seedPlants;
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
        this.allowedZombies = allowedZombies;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public void setWaves(List<Wave> waves) {
        this.waves = waves;
    }

    public LevelBehavior getBehavior() {
        return this.behavior;
    }

    public void setBehavior(LevelBehavior levelBehavior) {
        this.behavior = levelBehavior;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    // === Core Core-Data Records ===

    public record AllowedZombie(String id, int weight) {}

    public record Wave(int waveNumber, int cost, List<SpawnData> spawnData) {}

    public record SpawnData(String zombieId, int lane, float delaySeconds) {}
}