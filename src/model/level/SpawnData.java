package model.level;

public class SpawnData {
    private String zombieId;
    private int lane;
    private float delaySeconds;

    public String getZombieId() {
        return zombieId;
    }

    public void setZombieId(String zombieId) {
        this.zombieId = zombieId;
    }

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    public float getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(float delay) {
        this.delaySeconds = delay;
    }
}

