package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class SpinEffect implements EffectStatus {
    private static final double DEFAULT_SPIN_DURATION = 1.0;
    private static final double SPIN_SPEED_MULTIPLIER = 3.0;

    private double spinTimeRemaining = 0.0;
    private boolean currentlyBoosted = false;

    public void startSpin(double duration) {
        this.spinTimeRemaining = Math.max(this.spinTimeRemaining, duration);
    }

    public void startSpin() {
        startSpin(DEFAULT_SPIN_DURATION);
    }

    public boolean isSpinning() {
        return spinTimeRemaining > 0;
    }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) {
            if (currentlyBoosted) {
                revertSpeed(zombie);
                currentlyBoosted = false;
            }
            return;
        }

        boolean shouldBeBoosted = spinTimeRemaining > 0;

        if (shouldBeBoosted && !currentlyBoosted) {
            applyBoost(zombie);
            currentlyBoosted = true;
        } else if (!shouldBeBoosted && currentlyBoosted) {
            revertSpeed(zombie);
            currentlyBoosted = false;
        }

        if (spinTimeRemaining > 0) {
            spinTimeRemaining -= GameClock.SECONDS_PER_TICK;
        }
    }

    private void applyBoost(Zombie zombie) {
        Vec2 current = zombie.getSpeed();
        if (current == null) return;
        zombie.setSpeed(current.scale(SPIN_SPEED_MULTIPLIER));
    }

    private void revertSpeed(Zombie zombie) {
        Vec2 current = zombie.getSpeed();
        if (current == null) return;
        zombie.setSpeed(current.scale(1.0 / SPIN_SPEED_MULTIPLIER));
    }
}