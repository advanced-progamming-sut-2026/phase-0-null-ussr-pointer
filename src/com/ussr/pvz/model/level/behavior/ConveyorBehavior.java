package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.delivery.DeliveryStrategy;

public class ConveyorBehavior extends LevelBehavior {

    private double conveyorTimer = 0.0;
    private static final double CONVEYOR_INTERVAL = 12.0;
    private boolean initialDeliveryDone = false;

    @Override
    public void onStart(Level level) {
        super.onStart(level);
        level.setSunFalling(false);
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);

        if (session.isGameOver() || levelCompleted) return;

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
}