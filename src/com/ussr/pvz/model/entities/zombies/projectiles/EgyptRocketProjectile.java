package com.ussr.pvz.model.entities.zombies.projectiles;

import com.ussr.pvz.model.board.structures.GraveSpawner;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

import java.util.Random;

public class EgyptRocketProjectile extends MissileProjectile {
    private static final double FLIGHT_TIME = 1.4;
    private static final double LAUNCH_DELAY = 5.0; // tune to match ZombossEgypt's rocket-launch clip length

    public EgyptRocketProjectile(Vec2 startPos, Vec2 targetPos, int row, int col) {
        super(startPos, targetPos, FLIGHT_TIME, LAUNCH_DELAY, row, col, "ZombossEgypt");
    }

    @Override
    protected void applyDestinationEffect(GameSession session) {
        session.removePlantAt(targetCol, targetRow);

        Random rand = new Random();
        int gravesSpawned = 0;
        int maxAttempts = 20;

        while (gravesSpawned < 2 && maxAttempts > 0) {
            maxAttempts--;
            int r = rand.nextInt(session.getLawn().getRows());
            int c = rand.nextInt(session.getLawn().getCols());
            if (r == targetRow && c == targetCol) continue;

            if (GraveSpawner.spawnGraveIfEmpty(session, r, c)) {
                gravesSpawned++;
            }
        }
    }

    @Override
    public void onDestinationReached() {

    }

    @Override
    public String getPamLocation() {
        return "768/INITIAL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_EGYPT/ZOMBOSS_MISSILE_EXPLOSION_EGYPT.PAM";
    }
}