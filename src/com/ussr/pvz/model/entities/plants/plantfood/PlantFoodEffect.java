package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;

public interface PlantFoodEffect {
    // TODO: we should add other plant food effects like gain armor and etc and each has two method as you see one for the instant and one for the permanent
    // TODO: changes we should try to group as much plants as possible to have less plant food effect classes
    void triggerSuperpower(Plant user, GameSession session);

    void applyStatusModifiers(Plant user);
}
