package com.ussr.pvz.model.level;

import java.util.List;

public class Wave {
    private int waveNumber;
    private int cost;
    private List<SpawnData> spawnData;

    public int getWaveNumber() {
        return waveNumber;
    }

    public void setWaveNumber(int n) {
        this.waveNumber = n;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public List<SpawnData> getSpawnData() {
        return spawnData;
    }

    public void setSpawnData(List<SpawnData> s) {
        this.spawnData = s;
    }
}
