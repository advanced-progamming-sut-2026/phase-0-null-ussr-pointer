package com.ussr.pvz.model.entities.plants.plantfood.localattack;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;

public class GlobalShroomAttack extends LocalAttack {

    public GlobalShroomAttack(double duration, double strikeRate, String targetPlantName) {
        super(duration, strikeRate);
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        super.triggerSuperpower(user, session);

        if (session == null || session.getPlants() == null) return;
        //this may be 0.0
        user.setPlantFoodTimer(60.0);
    }
}
