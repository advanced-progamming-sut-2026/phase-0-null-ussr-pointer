package com.ussr.pvz.model.entities.zombies.defense;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class JesterDefense implements DefenseBehavior {
    private static final double MIRROR_SPEED = 20.0;

    @Override
    public int handleDamage(Zombie zombie, int rawDamage, Object damageSource, GameSession session) {
        if (damageSource instanceof Projectile projectile) {
            // Jester juggles standard straight shots and lobbed shots
            if (projectile.getMoveStrategy() instanceof StraightMove ||
                    projectile.getMoveStrategy() instanceof ArcMove) {

                mirrorBack(zombie, projectile, session);

                return 0; // Takes no damage from juggled objects
            }
        }
        return rawDamage;
    }

    private void mirrorBack(Zombie zombie, Projectile incoming, GameSession session) {
        if (session == null || zombie.getPosition() == null) return;

        Plant target = findNearestPlantInLane(zombie, session);
        if (target == null) return;

        // Zombies live at higher X than plants, so the mirrored shot travels
        // in the negative X direction, straight down the same row.
        Vec2 velocity = new Vec2(-MIRROR_SPEED, 0);

        session.getProjectiles().add(new Projectile(
                target,
                zombie.getPosition(),
                velocity,
                incoming.getDamage(),
                new StraightMove(),
                null
        ));
    }

    private Plant findNearestPlantInLane(Zombie zombie, GameSession session) {
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