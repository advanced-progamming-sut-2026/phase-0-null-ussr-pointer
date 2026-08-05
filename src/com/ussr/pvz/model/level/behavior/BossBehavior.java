package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossFactory;
import com.ussr.pvz.model.level.Level;

public class BossBehavior extends LevelBehavior {

    private static final String FALLBACK_ZOMBOSS_ALIAS = "ZombieGargantuar";

    @Override
    public void onStart(Level level) {
        super.onStart(level);

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return;

        String alias = resolveZombossAlias(level);
        int rows = session.getLawn().getRows();
        int primaryRow = Math.max(0, (rows / 2) - 1);
        int col = session.getLawn().getCols() - 1;

        ZombossFactory.spawn(alias, primaryRow, col, session);
    }

    private String resolveZombossAlias(Level level) {
        if (level.getAllowedZombies() == null || level.getAllowedZombies().isEmpty()) {
            return FALLBACK_ZOMBOSS_ALIAS;
        }
        return level.getAllowedZombies().get(0).id();
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {

    }

    @Override
    public boolean isFailed(Level level) {
        return false;
    }
}