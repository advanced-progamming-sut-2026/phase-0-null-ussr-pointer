package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

public class IceRocketProjectile extends MissileProjectile {
    private static final double FLIGHT_TIME = 1.4;
    private static final double LAUNCH_DELAY = 5.0; // tune to match ZombossMammoth's rocket-launch clip length

    public IceRocketProjectile(Vec2 startPos, Vec2 targetPos, int row, int col) {
        super(startPos, targetPos, FLIGHT_TIME, LAUNCH_DELAY, row, col, "ZombossMammoth");
    }

    @Override
    protected void applyDestinationEffect(GameSession session) {
        session.removePlantAt(targetCol, targetRow);
    }

    @Override
    public void onDestinationReached() {

    }

    @Override
    public String getPamLocation() {
        return "768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_ICEAGE/ZOMBOSS_MISSILE_EXPLOSION_ICEAGE.PAM";
    }
}