package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.ai.ZombieAIManager;

public abstract class LevelBehavior {

    protected ZombieAIManager aiManager;
    protected boolean autoWinOnWavesClear = true;
    protected boolean levelCompleted = false;

    public void onStart(Level level) {
        this.aiManager = new ZombieAIManager(App.getAccount().getDifficultyLvl());
    }

    public void onWaveComplete(Level level, int waveNumber) {
        GameSession session = App.getGameSession();
        if (session != null && level.getWaves() != null) {
            boolean isFinalWave = (waveNumber == level.getWaves().size());
            session.triggerWaveStart(waveNumber, isFinalWave);
        }
    }

    public void onComplete(Level level) {
        if (this.levelCompleted) return;
        this.levelCompleted = true;

        GameSession session = App.getGameSession();
        if (session != null) {
            session.getEventBus().publish(new GameEvent.WavesCompleted());
            session.getEventBus().publish(new GameEvent.GameWon());
        }
    }

    public boolean isFailed(Level level) {
        return false;
    }

    public void tick(GameSession session, double deltaTime) {
        if (levelCompleted) return;

        if (aiManager != null) {
            aiManager.tick(session, deltaTime);
        }

        checkLevelCompletion(session);
    }

    protected void checkLevelCompletion(GameSession session) {
        if (!autoWinOnWavesClear || aiManager == null) return;

        Level level = session.getLevel();
        if (level == null || level.getWaves() == null) return;

        if (aiManager.areAllWavesDone(level.getWaves()) && session.getZombies().isEmpty()) {
            onComplete(level);
        }
    }

    public void onZombieBreach(GameSession session, Zombie breachedZombie) {
        session.onZombieReachedEnd();
    }

    public void onPlantDied(GameSession session, Plant plant) {}

    public void onZombieDied(GameSession session, Zombie zombie) {}

    public void onSunCollected(GameSession session, int amount) {}

    public ZombieAIManager getAiManager() {
        return aiManager;
    }
}