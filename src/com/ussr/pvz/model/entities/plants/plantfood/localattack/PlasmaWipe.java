package com.ussr.pvz.model.entities.plants.plantfood.localattack;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodEffect;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class PlasmaWipe extends LocalAttack implements PlantFoodEffect {

    public PlasmaWipe(double duration, double strikeRate) {
        super(duration, strikeRate);
    }

    public void triggerSuperpower(Plant user , GameSession session) {
        super.triggerSuperpower(user , session);

        for (Zombie z : session.getZombies()) {
            z.setAlive(false);
        }
    }
}
