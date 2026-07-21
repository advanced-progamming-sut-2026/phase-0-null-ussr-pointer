package com.ussr.pvz.model.entities.plants.plantfood.localattack;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodEffect;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class SnowPeaAttack extends LocalAttack implements PlantFoodEffect{

    public SnowPeaAttack(double duration, double strikeRate) {
        super(duration, strikeRate);
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        super.triggerSuperpower(user, session);

        if (user == null || session == null || session.getZombies() == null) return;

        double plantY = user.getPosition().y();

        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive() && zombie.getPosition() != null) {
                if (Math.abs(zombie.getPosition().y() - plantY) <= 0.5) {
                    zombie.setStatus(Zombie.Status.FREEZE);
                }
            }
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        super.applyStatusModifiers(user);
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        super.tickDurationEffect(user , session , deltaTime);
    }
}
