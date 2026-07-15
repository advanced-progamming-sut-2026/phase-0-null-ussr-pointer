package com.ussr.pvz.model.entities.zombies.defense;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.effect.SpinEffect;
import com.ussr.pvz.model.util.Vec2;

public class JesterDefense implements DefenseBehavior {
    public static final double SPIN_DURATION_ON_DEFLECT = 1.0;

    @Override
    public int handleDamage(Zombie zombie, int rawDamage, Object damageSource, GameSession session) {
        // Reflection is now completely handled pre-impact inside Projectile.java's checkCollision().
        // If a projectile bypasses that check (e.g. Laser, AoE explosions), the Jester takes normal damage.
        return rawDamage;
    }

    public void triggerSpin(Zombie zombie) {
        if (zombie.getEffectStatus() instanceof SpinEffect spinEffect) {
            spinEffect.startSpin(SPIN_DURATION_ON_DEFLECT);
        }
    }

    public Plant findNearestPlantInLane(Zombie zombie, GameSession session) {
        double lane = zombie.getPosition().y();
        double zombieCol = zombie.getPosition().x();

        Plant nearest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Plant plant : session.getPlants()) {
            if (plant == null || !plant.isAlive() || plant.getLocation() == null) continue;
            if (Math.abs(plant.getLocation().y() - lane) >= 0.5) continue;
            if (plant.getLocation().x() >= zombieCol) continue;

            double distance = zombieCol - plant.getLocation().x();
            if (distance < closestDistance) {
                closestDistance = distance;
                nearest = plant;
            }
        }

        return nearest;
    }
}