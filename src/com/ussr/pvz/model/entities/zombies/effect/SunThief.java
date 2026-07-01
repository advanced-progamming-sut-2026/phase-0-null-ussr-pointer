package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.items.sun.SunToken;

public class SunThief implements EffectStatus {
    //todo
    // --- Configuration (Fed from JSON parsing) ---
    private final boolean isBankThief; // false for Ra, true for Turquoise/Crystal Skull
    private final int maxSunsToSteal;
    private final double dropRatioOnDeath;
    private final double chargingTime;
    private final int laserDamage;

    private int stolenSuns = 0;
    private boolean deathHandled = false;

    private double stateTimer = 0;
    private double oneSecondAccumulator = 0;
    private boolean isStealing = false;
    private boolean laserFired = false;

    public SunThief(boolean isBankThief, int maxSunsToSteal, double dropRatioOnDeath, double chargingTime, int laserDamage) {
        this.isBankThief = isBankThief;
        this.maxSunsToSteal = maxSunsToSteal;
        this.dropRatioOnDeath = dropRatioOnDeath;
        this.chargingTime = chargingTime;
        this.laserDamage = laserDamage;
    }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) {
            if (!deathHandled) {
                int returnAmount = (int) (stolenSuns * dropRatioOnDeath);
                if (returnAmount > 0) {
                    session.addSun(returnAmount);
                }
                deathHandled = true;
            }
            return;
        }

        if (isBankThief) {
            processBankThief(zombie, session);
        } else {
            processGroundThief(zombie, session);
        }
    }

    private void processGroundThief(Zombie zombie, GameSession session) {
        if (stolenSuns >= maxSunsToSteal) return;

        for (GroundItem item : session.getItems()) {
            if (stolenSuns >= maxSunsToSteal) break;

            if (item.isAlive() && item.getItemType() == ItemType.SUN) {
                int sunValue = 0;

                if (item instanceof ProducedSun producedSun) {
                    sunValue = producedSun.getValue();
                } else if (item instanceof SunToken sunToken) {
                    if (sunToken.isFalling()) continue;
                    sunValue = sunToken.getValue();
                }

                if (sunValue > 0) {
                    stolenSuns = Math.min(stolenSuns + sunValue, maxSunsToSteal);
                    item.setAlive(false);
                }
            }
        }
    }

    private void processBankThief(Zombie zombie, GameSession session) {
        if (laserFired) return;

        if (!isStealing) {
            if (canSeePlant(zombie, session)) {
                isStealing = true;
            }
        } else {
            stateTimer += GameClock.SECONDS_PER_TICK;
            oneSecondAccumulator += GameClock.SECONDS_PER_TICK;

            if (oneSecondAccumulator >= 1.0 && stateTimer <= chargingTime) {
                oneSecondAccumulator = 0;
                int amountToSteal = Math.min(25, session.getSunCount());
                if (amountToSteal > 0) {
                    session.spendSun(amountToSteal);
                    stolenSuns += amountToSteal;
                }
            }

            if (stateTimer >= chargingTime) {
                fireLaser(zombie, session);
                laserFired = true;
                isStealing = false;
            }
        }
    }

    private boolean canSeePlant(Zombie zombie, GameSession session) {
        int zCol = (int) zombie.getPosition().x();
        int zRow = (int) zombie.getPosition().y();

        for (int i = 1; i <= 4; i++) {
            int targetCol = zCol - i;
            if (targetCol >= 0) {
                Cell cell = session.getLawn().getCell(zRow, targetCol);
                if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void fireLaser(Zombie zombie, GameSession session) {
        int zCol = (int) zombie.getPosition().x();
        int zRow = (int) zombie.getPosition().y();

        for (int i = 1; i <= 4; i++) {
            int targetCol = zCol - i;
            if (targetCol >= 0) {
                Cell cell = session.getLawn().getCell(zRow, targetCol);
                if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
                    cell.getPlant().takeDamage(laserDamage,zombie);
                }
            }
        }
    }
}