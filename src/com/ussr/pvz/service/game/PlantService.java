package com.ussr.pvz.service.game;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PlantService {

    public Zombie findTargetInLane(Plant plant, GameSession session) {
        if (plant == null || plant.getLocation() == null || session == null) return null;

        int lane = plant.getLocation().y();
        double plantX = plant.getLocation().x();

        return session.getZombies().stream()
                .filter(z -> z != null && z.isAlive() && z.getPosition() != null)
                .filter(z -> (int) z.getPosition().y() == lane)
                .filter(z -> z.getPosition().x() >= plantX)
                .min(Comparator.comparingDouble(z -> z.getPosition().x() - plantX))
                .orElse(null);
    }

    public List<Zombie> findZombiesInRange(Plant plant, double radius, GameSession session) {
        if (plant == null || plant.getLocation() == null || session == null) return List.of();

        Vec2 plantPos = Vec2.of(plant.getLocation().x(), plant.getLocation().y());

        return session.getZombies().stream()
                .filter(z -> z != null && z.isAlive() && z.getPosition() != null)
                .filter(z -> z.getPosition().distanceTo(plantPos) <= radius)
                .collect(Collectors.toList());
    }

    public void produceSun(Plant plant, GameSession session, int sunAmount) {
        if (plant == null || plant.getLocation() == null || session == null) return;

        int x = plant.getLocation().x();
        int y = plant.getLocation().y();

        ProducedSun sun = new ProducedSun(x, y, sunAmount);
        session.getItems().add(sun);
    }

    public void fireProjectile(Plant plant, GameSession session, Projectile projectile) {
        if (plant == null || plant.getLocation() == null || session == null || projectile == null) return;

        session.getProjectiles().add(projectile);
        session.getEventBus().publish(new GameEvent.ProjectileFired(
                plant.getName(), plant.getLocation().x(), plant.getLocation().y()
        ));
    }

    public void explode(Plant plant, GameSession session, int damage, double radius) {
        if (plant == null || session == null) return;

        List<Zombie> targets = findZombiesInRange(plant, radius, session);
        for (Zombie zombie : targets) {
            zombie.takeDamage(damage);
        }

        // Destroy the plant itself after the explosion finishes
        plant.takeDamage(plant.getHp());
    }

    public void processMeleeAttack(Plant plant, GameSession session, int damage, double range) {
        if (plant == null || session == null) return;

        Zombie target = findTargetInLane(plant, session);
        if (target != null) {
            double distance = target.getPosition().x() - plant.getLocation().x();
            if (distance <= range) {
                target.takeDamage(damage);
            }
        }
    }
}