package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

public class BabySharkProjectile extends ZombieBossProjectile {
    private static final double FLIGHT_TIME = 1.0;

    private final int targetRow;
    private final int targetCol;

    public BabySharkProjectile(Vec2 startPos, Vec2 targetPos, int row, int col) {
        super(startPos, targetPos, FLIGHT_TIME, "ZombossShark");
        this.targetRow = row;
        this.targetCol = col;
    }

    @Override
    protected void applyDestinationEffect(GameSession session) {
        if (session.getLawn().getTile(targetRow, targetCol) != null && session.getLawn().getTile(targetRow, targetCol).getType() == TileType.Water) {
            session.removePlantAt(targetCol, targetRow);
        }
    }

    @Override
    public void onDestinationReached() {

    }

    @Override
    public String getPamLocation() {
        return "768/FULL/EFFECTS/ZOMBOSS_SHARK_PROJECTILE/ZOMBOSS_SHARK_PROJECTILE.PAM";
    }
}