package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.projectiles.GargantuarImpProjectile;
import com.ussr.pvz.model.util.Vec2;

/**
 * Spec p.34: whenever the Gargantuar reaches half its own health, it throws
 * the Imp from behind it, landing it in the 3rd column from the left of its
 * own row. Reads ImpApex/ImpFlightTime/ImpTargetColumn/MinPosXThrowImp from
 * zombies.json via the params passed in by EffectStatusRegistry.
 */
public class GargantuarImpThrowEffect implements EffectStatus {
    private final double healthPercentThrowImp;
    private final double impApex;
    private final double impFlightTime;
    private final int impTargetColumn;
    private final double minPosXThrowImp;
    private final String impAlias;

    private boolean impThrown = false;

    public GargantuarImpThrowEffect(double healthPercentThrowImp, double impApex, double impFlightTime,
                                    int impTargetColumn, double minPosXThrowImp, String impAlias) {
        this.healthPercentThrowImp = healthPercentThrowImp;
        this.impApex = impApex;
        this.impFlightTime = impFlightTime;
        this.impTargetColumn = impTargetColumn;
        this.minPosXThrowImp = minPosXThrowImp;
        this.impAlias = impAlias;
    }

    @Override
    public void effect(Zombie gargantuar, GameSession session) {
        if (impThrown || !gargantuar.isAlive()) return;
        if (gargantuar.getMaxHp() <= 0) return;

        double healthRatio = (double) gargantuar.getHp() / gargantuar.getMaxHp();
        if (healthRatio > healthPercentThrowImp) return;

        // MinPosXThrowImp is a pixel-space guard in the original game (don't
        // throw the imp before the Gargantuar has walked onto the lawn at
        // all); here position.x() is in grid units, so only gate on it when
        // a meaningful (positive) value was supplied.
        if (minPosXThrowImp > 0 && gargantuar.getPosition().x() * 100.0 < minPosXThrowImp) {
            return;
        }

        throwImp(gargantuar, session);
        impThrown = true;
    }

    private void throwImp(Zombie gargantuar, GameSession session) {
        int row = (int) gargantuar.getPosition().y();
        if (session.getLawn() == null) return;

        int cols = session.getLawn().getCols();
        int targetCol = Math.min(impTargetColumn, cols - 1);

        Vec2 startPos = gargantuar.getPosition();
        Vec2 targetPos = Vec2.of(targetCol, row);

        session.addZombieProjectile(new GargantuarImpProjectile(
                startPos, targetPos, impFlightTime, impApex, row, impAlias
        ));
    }
}