package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Level;

public class PlantWhatYouGetBehavior extends LevelBehavior {

    private int startingSun = 800;

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
}