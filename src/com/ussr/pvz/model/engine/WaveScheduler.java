package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.SpawnData;
import com.ussr.pvz.model.level.Wave;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class WaveScheduler {

    private final List<ScheduledSpawn> pending = new ArrayList<>();
    private int nextIndex = 0;
    private static final Random RAND = new Random();

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

    public void load(Level level, int lawnRows, int lawnCols) {
        pending.clear();
        nextIndex = 0;

        if (level == null || level.getWaves() == null) return;

        final double WAVE_GAP_SECONDS = 25.0;

        for (Wave wave : level.getWaves()) {
            double waveOffset = (wave.getWaveNumber() - 1) * WAVE_GAP_SECONDS;

            if (wave.getSpawnData() != null) {
                for (SpawnData spawn : wave.getSpawnData()) {
                    pending.add(new ScheduledSpawn(
                            waveOffset + spawn.getDelaySeconds(),
                            spawn.getZombieId(),
                            spawn.getLane(),
                            lawnCols
                    ));
                }
            }

            if (wave.getCost() > 0 && level.getAllowedZombies() != null) {
                generateDynamicSpawns(wave.getCost(), waveOffset, level.getAllowedZombies(), lawnRows, lawnCols);
            }
        }

        pending.sort(Comparator.comparingDouble(s -> s.triggerSeconds));
    }

    private void generateDynamicSpawns(int budget, double waveOffset, List<Level.AllowedZombie> allowed, int maxRows, int spawnCol) {
        int remainingCost = budget;

        while (remainingCost > 0) {
            final int currentBudget = remainingCost;

            List<Level.AllowedZombie> purchasable = allowed.stream()
                    .filter(z -> ZombieFactory.getZombieCost(z.id()) <= currentBudget)
                    .toList();

            if (purchasable.isEmpty()) break;

            int totalWeight = purchasable.stream().mapToInt(Level.AllowedZombie::weight).sum();
            int roll = RAND.nextInt(totalWeight);

            String selectedZombieId = null;
            int currentWeight = 0;
            for (Level.AllowedZombie az : purchasable) {
                currentWeight += az.weight();
                if (roll < currentWeight) {
                    selectedZombieId = az.id();
                    break;
                }
            }

            if (selectedZombieId == null) break;

            remainingCost -= ZombieFactory.getZombieCost(selectedZombieId);

            int lane = RAND.nextInt(maxRows);
            double randomDelay = RAND.nextDouble() * 15.0;

            pending.add(new ScheduledSpawn(
                    waveOffset + randomDelay,
                    selectedZombieId,
                    lane,
                    spawnCol
            ));
        }
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