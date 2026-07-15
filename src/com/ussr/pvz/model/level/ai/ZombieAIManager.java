package com.ussr.pvz.model.level.ai;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Level;
import java.util.ArrayList;
import java.util.List;

public class ZombieAIManager {
    private final List<WaveDirector> activeDirectors = new ArrayList<>();
    private int nextWaveIndexToSpawn = 0;

    private final Difficulty difficulty;
    private final double costMultiplier;

    public ZombieAIManager(int difficultyLevel) {
        // Rule: Map integer levels directly to Behavior Enum
        this.difficulty = switch (difficultyLevel) {
            case 1      -> Difficulty.SIMPLE;
            case 2, 3   -> Difficulty.MEDIUM;
            case 4, 5   -> Difficulty.HARD;
            default     -> Difficulty.MEDIUM;
        };

        // Rule: Scale point budgets larger based on the exact intensity level selected
        this.costMultiplier = switch (difficultyLevel) {
            case 1 -> 1.0;  // Baseline budget
            case 2 -> 1.25; // +25% points
            case 3 -> 1.50; // +50% points
            case 4 -> 1.75; // +75% points
            case 5 -> 2.00; // Double the wave budget size!
            default -> 1.0;
        };
    }

    public void tick(GameSession session, double deltaTime) {
        if (!session.isWavesStarted() || session.isGameOver()) return;

        Level level = session.getLevel();
        if (level == null) return;

        List<Level.Wave> waves = level.getWaves();
        if (waves == null || waves.isEmpty()) return;

        // 1. Kick off the first wave if none are active and we have waves left
        if (activeDirectors.isEmpty() && nextWaveIndexToSpawn < waves.size()) {
            spawnNextWaveDirector(waves, session);
        }

        // 2. Tick all active wave directors safely
        for (WaveDirector director : activeDirectors) {
            director.tick(session, deltaTime);
        }

        // 3. Check if the leading (latest) wave is ready for the next wave to start/overlap
        if (!activeDirectors.isEmpty()) {
            WaveDirector leadingWave = activeDirectors.get(activeDirectors.size() - 1);
            if (leadingWave.isReadyForNextWave() && nextWaveIndexToSpawn < waves.size()) {
                spawnNextWaveDirector(waves, session);
            }
        }

        // 4. Clean up any directors that have exhausted their budget and cleared their spawned zombies
        activeDirectors.removeIf(WaveDirector::isFullyCleared);
    }

    private void spawnNextWaveDirector(List<Level.Wave> waves, GameSession session) {
        Level.Wave waveData = waves.get(nextWaveIndexToSpawn);

        // Trigger the wave start announcement event and execute active Chapter Effects
        int waveNumber = waveData.waveNumber();
        boolean isFinalWave = (nextWaveIndexToSpawn == waves.size() - 1);
        session.triggerWaveStart(waveNumber, isFinalWave);

        // Instantiate and track the new WaveDirector using the optimized strategy-cached constructor
        WaveDirector newWave = new WaveDirector(nextWaveIndexToSpawn, waveData, difficulty, costMultiplier);
        activeDirectors.add(newWave);
        nextWaveIndexToSpawn++;
    }

    public boolean areAllWavesDone(List<Level.Wave> waves) {
        return nextWaveIndexToSpawn >= waves.size() && activeDirectors.isEmpty();
    }
}