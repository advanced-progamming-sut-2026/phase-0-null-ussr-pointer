package com.ussr.pvz.model.entities.zombies.attack;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class SmashAttack implements AttackBehavior {
    private final int smashDamage;
    private final double windupDuration;
    private final boolean isOneTime;
    private final double speedScaleAfter;

    private double timer = 0.0;

    /**
     * @param smashDamage     The damage dealt (e.g., 1500 for Gargantuar/All-Star)
     * @param windupDuration  Delay before the hit (e.g., 2.0 for Gargantuar, 0.0 for All-Star)
     * @param isOneTime       If true, the attack is discarded after one use (All-Star tackle)
     * @param speedScaleAfter Speed multiplier applied after a one-time use (e.g., 0.5 for All-Star)
     */
    public SmashAttack(int smashDamage, double windupDuration, boolean isOneTime, double speedScaleAfter) {
        this.smashDamage = smashDamage;
        this.windupDuration = windupDuration;
        this.isOneTime = isOneTime;
        this.speedScaleAfter = speedScaleAfter;
    }

    @Override
    public void attack(Zombie zombie, GameSession session) {
        Damageable target = zombie.acquireTarget(session);

        // If the target dies before the windup finishes (e.g., shot by a peashooter), reset the swing.
        if (target == null || !target.isAlive()) {
            timer = 0;
            return;
        }

        timer += GameClock.SECONDS_PER_TICK;

        if (timer >= windupDuration) {
            // Apply the massive smash damage
            if (target instanceof Plant p) {
                p.takeDamage(smashDamage, zombie);
            } else {
                target.takeDamage(smashDamage);
            }

            timer = 0;

            // Handle All-Star tackle exhaustion
            if (isOneTime) {
                // Permanently slow the zombie down
                if (zombie.getSpeed() != null) {
                    zombie.setSpeed(zombie.getSpeed().scale(speedScaleAfter));
                }

                // Fall back to a standard bite attack for the rest of the game
                zombie.setAttackBehavior(new ChompAttack());
            }
        }
    }
}