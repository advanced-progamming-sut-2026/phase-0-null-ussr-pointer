package com.ussr.pvz.model.level;

import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.level.delivery.DeliveryStrategy;

import java.util.ArrayList;
import java.util.List;

public class Level {

    private String id;
    private int order;
    private String chapter;

    private boolean sunFalling = true;
    private int timeLimitSeconds = 0;
    private int deadlineColumn = -1;
    private int allowedPlantsLost = -1;
    private List<String> lockedPlants = new ArrayList<>();
    private List<String> seedPlants = new ArrayList<>();

    private DeliveryStrategy deliveryStrategy;

    private List<String> allowedZombies;
    private List<Wave> waves;

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

    public boolean isFailed() {
        return behavior != null && behavior.isFailed(this);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setSunFalling(boolean sunFalling) {
        this.sunFalling = sunFalling;
    }

    public void setTimeLimitSeconds(int timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public void setDeadlineColumn(int deadlineColumn) {
        this.deadlineColumn = deadlineColumn;
    }

    public void setAllowedPlantsLost(int allowedPlantsLost) {
        this.allowedPlantsLost = allowedPlantsLost;
    }

    public void setLockedPlants(List<String> lockedPlants) {
        this.lockedPlants = lockedPlants;
    }

    public void setBehavior(LevelBehavior levelBehavior) {
        this.behavior = levelBehavior;
    }

    public void setDeliveryStrategy(DeliveryStrategy deliveryStrategy) {
        this.deliveryStrategy = deliveryStrategy;
    }

    public DeliveryStrategy getDeliveryStrategy() {
        return deliveryStrategy;
    }

    public void setAllowedZombies(List<String> allowedZombies) {
        this.allowedZombies = allowedZombies;
    }

    public List<String> getAllowedZombies() {
        return allowedZombies;
    }

    public void setWaves(List<Wave> waves) {
        this.waves = waves;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public String getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public boolean isSunFalling() {
        return sunFalling;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public int getDeadlineColumn() {
        return deadlineColumn;
    }

    public int getAllowedPlantsLost() {
        return allowedPlantsLost;
    }

    public List<String> getLockedPlants() {
        return lockedPlants;
    }

    public List<String> getSeedPlants() {
        return seedPlants;
    }

    public void setSeedPlants(List<String> seedPlants) {
        this.seedPlants = seedPlants;
    }

    public Object getBehavior() {
        return this.behavior;
    }
}

