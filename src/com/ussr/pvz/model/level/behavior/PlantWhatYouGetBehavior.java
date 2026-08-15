package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PlantWhatYouGetBehavior extends LevelBehavior {

    private int startingSun = 800;

    /**
     * Snapshot of each account plant's recharge value taken just before
     * we zero them all out in onStart. Keyed by the Plant instance itself
     * so we don't need to rely on name uniqueness.
     */
    private final Map<Plant, Double> originalRecharges = new HashMap<>();

    public PlantWhatYouGetBehavior() {
        this.waitForManualWaveStart = true;
    }

    public void setStartingSun(int startingSun) {
        this.startingSun = startingSun;
    }

    @Override
    public void onStart(Level level) {
        super.onStart(level);

        GameSession session = App.getGameSession();
        if (session != null) {
            // Snapshot recharges BEFORE zeroing them
            originalRecharges.clear();
            if (App.getAccount() != null) {
                for (Plant plant : App.getAccount().getAdventureProgress().getAccountPlants()) {
                    originalRecharges.put(plant, plant.getRecharge());
                }
            }

            session.removeAllCooldowns();
            session.addSun(startingSun - session.getSunCount());
            level.setSunFalling(false);
        }
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);

        if (levelCompleted || session.isGameOver()) return;
        if (!session.isWavesStarted()) {
            session.removeAllCooldowns();
        }
    }

    @Override
    public void onComplete(Level level) {
        // Restore recharges before the super call fires WavesCompleted and
        // advances to the next level — so the next level starts clean.
        restoreRecharges();
        super.onComplete(level);
    }

    /**
     * Also restore on defeat so the account plants are not permanently
     * corrupted if the player loses and retries from the menu.
     */
    @Override
    public boolean isFailed(Level level) {
        boolean failed = super.isFailed(level);
        if (failed) {
            restoreRecharges();
        }
        return failed;
    }

    private void restoreRecharges() {
        if (originalRecharges.isEmpty()) return;
        for (Map.Entry<Plant, Double> entry : originalRecharges.entrySet()) {
            entry.getKey().setRecharge(entry.getValue());
        }
        originalRecharges.clear();
    }
}