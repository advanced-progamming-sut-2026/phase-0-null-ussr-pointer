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

    private static final String EGYPT_CHAPTER_ID = "egypt-1"; // match your levels.json id
    private static final int SANDSTORM_MIN_OFFSET = 1;
    private static final int SANDSTORM_MAX_OFFSET = 4;

    private final List<ScheduledSpawn> pending = new ArrayList<>();
    private int nextIndex = 0;
    private int lastAnnouncedWave = 0;
    private int finalWaveNumber = 0;
    private static final Random RAND = new Random();

    public static class ScheduledSpawn {
        public final double triggerSeconds;
        public final String zombieId;
        public final int lane;
        public final int spawnCol;
        public final int waveNumber;

        public ScheduledSpawn(double triggerSeconds, String zombieId, int lane, int spawnCol, int waveNumber) {
            this.triggerSeconds = triggerSeconds;
            this.zombieId = zombieId;
            this.lane = lane;
            this.spawnCol = spawnCol;
            this.waveNumber = waveNumber;
        }
    }

    public void load(Level level, int lawnRows, int lawnCols) {
        pending.clear();
        nextIndex = 0;
        lastAnnouncedWave = 0;
        finalWaveNumber = 0;

        if (level == null || level.getWaves() == null) return;

        final double WAVE_GAP_SECONDS = 25.0;
        boolean isEgypt = EGYPT_CHAPTER_ID.equals(level.getChapterId());

        finalWaveNumber = level.getWaves().stream()
                .mapToInt(Wave::getWaveNumber)
                .max()
                .orElse(0);

        for (Wave wave : level.getWaves()) {
            double waveOffset = (wave.getWaveNumber() - 1) * WAVE_GAP_SECONDS;
            boolean isFinalWave = wave.getWaveNumber() == finalWaveNumber;
            boolean applySandstorm = isEgypt && isFinalWave; // Part D

            if (wave.getSpawnData() != null) {
                for (SpawnData spawn : wave.getSpawnData()) {
                    int col = applySandstorm ? sandstormColumn(lawnCols) : lawnCols;
                    pending.add(new ScheduledSpawn(
                            waveOffset + spawn.getDelaySeconds(),
                            spawn.getZombieId(),
                            spawn.getLane(),
                            col,
                            wave.getWaveNumber()
                    ));
                }
            }

            if (wave.getCost() > 0 && level.getAllowedZombies() != null) {
                generateDynamicSpawns(wave.getCost(), waveOffset, level.getAllowedZombies(),
                        lawnRows, lawnCols, wave.getWaveNumber(), applySandstorm);
            }
        }

        pending.sort(Comparator.comparingDouble(s -> s.triggerSeconds));
    }

    private int sandstormColumn(int normalCol) {
        int offset = SANDSTORM_MIN_OFFSET + RAND.nextInt(SANDSTORM_MAX_OFFSET - SANDSTORM_MIN_OFFSET + 1);
        return Math.max(0, normalCol - offset);
    }

    private void generateDynamicSpawns(int budget, double waveOffset, List<Level.AllowedZombie> allowed,
                                       int maxRows, int spawnCol, int waveNumber, boolean applySandstorm) {
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
            int col = applySandstorm ? sandstormColumn(spawnCol) : spawnCol;

            pending.add(new ScheduledSpawn(
                    waveOffset + randomDelay,
                    selectedZombieId,
                    lane,
                    col,
                    waveNumber
            ));
        }
    }

    public void tick(GameSession session, double elapsedSeconds) {
        while (nextIndex < pending.size()) {
            ScheduledSpawn next = pending.get(nextIndex);
            if (elapsedSeconds < next.triggerSeconds) break;

            if (next.waveNumber > lastAnnouncedWave) {
                lastAnnouncedWave = next.waveNumber;
                session.triggerWaveStart(lastAnnouncedWave, lastAnnouncedWave == finalWaveNumber);
            }

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