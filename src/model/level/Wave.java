package model.level;

import java.util.List;

public class Wave {
    private int            waveNumber;
    private List<SpawnData> spawnData;

    public int             getWaveNumber()               { return waveNumber; }
    public void            setWaveNumber(int n)          { this.waveNumber = n; }
    public List<SpawnData> getSpawnData()                { return spawnData; }
    public void            setSpawnData(List<SpawnData> s) { this.spawnData = s; }
}
