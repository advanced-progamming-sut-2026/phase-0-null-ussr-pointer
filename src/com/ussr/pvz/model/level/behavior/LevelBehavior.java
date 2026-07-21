package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.NewsObserver;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.ai.ZombieAIManager;
import com.ussr.pvz.service.SaveService;

import java.util.List;

public abstract class LevelBehavior {

    protected ZombieAIManager aiManager;
    protected boolean autoWinOnWavesClear = true;
    protected boolean levelCompleted = false;

    protected boolean waitForManualWaveStart = false;

    public void onStart(Level level) {
        if(level.getBehavior() instanceof VaseBreakerBehavior){
            return;
        }
        this.aiManager = new ZombieAIManager(App.getAccount().getDifficultyLvl());
        GameSession session = App.getGameSession();
        if (session != null && !waitForManualWaveStart && !session.isWavesStarted()) {
            session.startWaves();
        }
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

        Account account = App.getAccount();
        if (account != null && level != null && level.getId() != null) {
            // Check if the level has NOT been completed before
            boolean isFirstTimeClear = !account.getAdventureProgress().isLevelCompleted(level.getId());

            if (isFirstTimeClear) {
                // 1. Record completed level ID
                account.getAdventureProgress().addCompletedLevel(level.getId());

                // 2. Trigger news item ONLY on first-time completion
                NewsObserver.triggerNewLevel(level);

                // 3. Save updated accounts state to disk
                List<AccountState> updatedStates = App.getAccounts().stream()
                        .map(Account::toState)
                        .toList();
                SaveService.saveAccounts(updatedStates);
            }
        }

        // 4. Dispatch game completion events to event bus
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