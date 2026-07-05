package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class LocalAttack implements PlantFoodEffect {
    private final double duration;
    private final double strikeRate;
    private final int damagePerStrike;

    public LocalAttack(double duration, double strikeRate, int damagePerStrike) {
        this.duration = duration;
        this.strikeRate = strikeRate;
        this.damagePerStrike = damagePerStrike;
    }
    @Override
    public void triggerSuperpower(Plant user, GameSession session) {

        user.setPlantFoodTimer(this.duration);
        user.setInternalTimer(0.0);
    }

    @Override
    public void applyStatusModifiers(Plant user) {

    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        user.setInternalTimer(user.getIntervalTimer() - deltaTime);

        if (user.getIntervalTimer() > 0) return;
        user.setInternalTimer(this.strikeRate);

        if (session.getZombies() == null || session.getZombies().isEmpty()) return;

        double plantX = user.getPosition().x();
        double plantY = user.getPosition().y();

        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive()) {
                boolean in3x3Range = Math.abs(zombie.getPosition().x() - plantX) <= 1.0
                        && Math.abs(zombie.getPosition().y() - plantY) <= 1.0;

                 if (in3x3Range) {
                     zombie.takeDamage(this.damagePerStrike, user);
                 }
            }
        }

    }
}
