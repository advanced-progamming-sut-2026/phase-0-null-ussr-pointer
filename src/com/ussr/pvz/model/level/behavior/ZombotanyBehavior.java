package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.level.Level;

public class ZombotanyBehavior extends LevelBehavior {

    // TODO: The Zombotany Minigame Mechanics
    //  1. Create a custom Zombie spawn rule here or in the WaveDirector that forces
    //  zombies to spawn with a PeashooterZombieEffect.
    //  2. Ensure these custom zombies utilize a ShootStrategy so they fire projectiles
    //  at the player's plants while walking left.

    public ZombotanyBehavior() {
    }

    @Override
    public void onStart(Level level) {
        // Standard setup: sun falling and normal wave scheduling applies.
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
    }

    @Override
    public void onComplete(Level level) {
    }

    @Override
    public boolean isFailed(Level level) {
        // Handled by the standard zombie-reach-house GameSession logic
        return false;
    }
}