package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class Area3x3Attack implements PlantFoodEffect {
    private final double tileRadius;
    private final double duration;
    private final double strikeRate;
    private double strikeTimer = 0.0;

    public Area3x3Attack(double tileRadius, double duration, double strikeRate) {
        this.tileRadius = tileRadius;
        this.duration = duration;
        this.strikeRate = strikeRate;
    }

    public Area3x3Attack(double duration, double strikeRate) {
        System.out.println("area");
        this(1, duration, strikeRate);
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (user != null) {
            user.setPlantFoodTimer(this.duration);
            this.strikeTimer = 0.0;
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        // No permanent stat modifiers needed
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        if (user == null || session == null || session.getZombies() == null) return;

        if (user.getPlantFoodTimer() <= 0) return;

        strikeTimer += deltaTime;

        while (strikeRate > 0 && strikeTimer >= strikeRate) {
            strikeTimer -= strikeRate;
            performAreaPulse(user, session);
        }
    }

    private void performAreaPulse(Plant user, GameSession session) {
        int userLane = (int) user.getPosition().y();
        double userX = user.getPosition().x();

        int damage = duration < 0.5 ? 1000 : user.getDamage();

        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive()) {
                double zombieLane = zombie.getPosition().y();
                double zombieX = zombie.getPosition().x();

                boolean isLaneInRange = Math.abs(zombieLane - userLane) <= 1;
                boolean isXInRange = Math.abs(zombieX - userX) <= this.tileRadius;

                if (isLaneInRange && isXInRange) {
                    zombie.takeDamage(damage , user);
                }
            }
        }
    }
}