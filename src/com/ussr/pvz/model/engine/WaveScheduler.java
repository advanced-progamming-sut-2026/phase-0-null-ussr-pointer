package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.SpawnData;
import com.ussr.pvz.model.level.Wave;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WaveScheduler {

    private final List<ScheduledSpawn> pending = new ArrayList<>();
    private int nextIndex = 0;

    public static class ScheduledSpawn {
        public final double triggerSeconds;
        public final String zombieId;
        public final int lane;
        public final int spawnCol;

        public ScheduledSpawn(double triggerSeconds, String zombieId, int lane, int spawnCol) {
            this.triggerSeconds = triggerSeconds;
            this.zombieId = zombieId;
            this.lane = lane;
            this.spawnCol = spawnCol;
        }
    }

    public void load(List<Wave> waves, int lawnCols) {
        pending.clear();
        nextIndex = 0;

        if (waves == null) return;

        final double WAVE_GAP_SECONDS = 20.0;

        for (Wave wave : waves) {
            if (wave.getSpawnData() == null) continue;
            double waveOffset = (wave.getWaveNumber() - 1) * WAVE_GAP_SECONDS;

            for (SpawnData spawn : wave.getSpawnData()) {
                double triggerTime = waveOffset + spawn.getDelaySeconds();
                pending.add(new ScheduledSpawn(
                        triggerTime,
                        spawn.getZombieId(),
                        spawn.getLane(),
                        lawnCols
                ));
            }
        }

        pending.sort(Comparator.comparingDouble(s -> s.triggerSeconds));
    }

    public void tick(GameSession session, double elapsedSeconds) {
        while (nextIndex < pending.size()) {
            ScheduledSpawn next = pending.get(nextIndex);
            if (elapsedSeconds < next.triggerSeconds) break;

            try {
                Zombie zombie = ZombieFactory.create(next.zombieId, next.lane, next.spawnCol);
                session.spawnZombie(zombie);
            } catch (IllegalArgumentException e) {
                System.err.println("[WaveScheduler] Unknown zombie id: " + next.zombieId);
            }

            nextIndex++;
        }
    }

    public boolean isDone() {
        return nextIndex >= pending.size();
    }

    public int getRemainingSpawns() {
        return pending.size() - nextIndex;
    }
}