package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

public class IceRocketProjectile extends ZombieBossProjectile {
    private static final double FLIGHT_TIME = 1.4;

    private final int targetRow;
    private final int targetCol;

    public IceRocketProjectile(Vec2 startPos, Vec2 targetPos, int row, int col) {
        super(startPos, targetPos, FLIGHT_TIME, "ZombossMammoth");
        this.targetRow = row;
        this.targetCol = col;
    }

    @Override
    protected void applyDestinationEffect(GameSession session) {
        session.removePlantAt(targetCol, targetRow);
    }

    @Override
    public void onDestinationReached() {

    }
}