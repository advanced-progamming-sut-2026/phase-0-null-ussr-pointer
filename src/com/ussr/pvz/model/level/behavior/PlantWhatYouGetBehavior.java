package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Level;

public class PlantWhatYouGetBehavior implements LevelBehavior {

    private int startingSun = 800; // Default, can be configured via JSON

    public PlantWhatYouGetBehavior() {
    }

    public void setStartingSun(int startingSun) {
        this.startingSun = startingSun;
    }

    @Override
    public void onStart(Level level) {
        GameSession session = App.getGameSession();
        if (session != null) {
            // 1. Set the initial sun count (e.g., 500 or 800)
            int currentSun = session.getSunCount();
            session.addSun(startingSun - currentSun); // Adjusts strictly to startingSun

            // 2. Disable falling sun from the sky
            level.setSunFalling(false);

            // 3. Remove all cooldowns during the setup phase
            session.removeAllCooldowns();

            // Note: Banning Sunflowers should be handled by LevelFactory adding
            // them to level.getLockedPlants() during JSON parsing.
        }
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
        // Typically, no sun is rewarded, but you can hook into this if needed
    }

    @Override
    public void onComplete(Level level) {
    }

    @Override
    public boolean isFailed(Level level) {
        return false; // Relies on the standard Game Over (zombies reaching the house)
    }
}