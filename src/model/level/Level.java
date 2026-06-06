package model.level;

import model.level.delivery.DeliveryStrategy;

import java.util.List;

public abstract class Level {

    protected String id;
    protected int order;
    protected List<Wave> waves;
    protected List<String> allowedZombies;
    protected DeliveryStrategy deliveryStrategy;

    public abstract void onStart();

    public abstract void onWaveComplete(int waveNumber);

    public abstract void onComplete();

    public abstract boolean isBossLevel();

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

    public List<Wave> getWaves() {
        return waves;
    }

    public void setWaves(List<Wave> waves) {
        this.waves = waves;
    }

    public DeliveryStrategy getDeliveryStrategy() {
        return deliveryStrategy;
    }

    public void setDeliveryStrategy(DeliveryStrategy ds) {
        this.deliveryStrategy = ds;
    }

    public List<String> getAllowedZombies() {
        return allowedZombies;
    }

    public void setAllowedZombies(List<String> list) {
        this.allowedZombies = list;
    }
}

