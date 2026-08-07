package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossFactory;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.delivery.DeliveryStrategy;

public class BossBehavior extends LevelBehavior {

    private static final double CONVEYOR_INTERVAL = 12.0;

    private ZombossController controller;
    private boolean bossSpawned = false;
    private boolean initialDeliveryDone = false;
    private double conveyorTimer = 0.0;

    public BossBehavior() {
        this.autoWinOnWavesClear = false;
    }

    @Override
    public void onStart(Level level) {
        super.onStart(level);

        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null || bossSpawned) return;

        String alias = level.getZombossAlias();
        if (alias == null || alias.isBlank()) {
            System.err.println("[BossBehavior] Level " + level.getId()
                    + " uses BossBehavior but has no \"zombossAlias\" configured.");
            return;
        }

        int rows = session.getLawn().getRows();
        int cols = session.getLawn().getCols();

        int primaryRow = Math.max(0, (rows / 2) - 1);
        int spawnCol = cols - 1;

        try {
            this.controller = ZombossFactory.spawn(alias, primaryRow, spawnCol, session);
            this.bossSpawned = true;
        } catch (Exception e) {
            System.err.println("[BossBehavior] Failed to spawn zomboss \"" + alias + "\": " + e.getMessage());
        }
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        if (levelCompleted) return;

        if (aiManager != null) {
            aiManager.tick(session, deltaTime);
        }

        tickDelivery(session, deltaTime);

        if (controller != null && controller.getCurrentHp() <= 0) {
            onComplete(session.getLevel());
        }
    }

    private void tickDelivery(GameSession session, double deltaTime) {
        Level level = session.getLevel();
        if (level == null) return;

        DeliveryStrategy strategy = level.getDeliveryStrategy();
        if (strategy == null) return;

        if (!initialDeliveryDone) {
            strategy.onLevelStart();
            initialDeliveryDone = true;
        }

        conveyorTimer += deltaTime;
        if (conveyorTimer >= CONVEYOR_INTERVAL) {
            conveyorTimer = 0.0;
            strategy.deliver();
        }
    }

    public ZombossController getController() {
        return controller;
    }
}