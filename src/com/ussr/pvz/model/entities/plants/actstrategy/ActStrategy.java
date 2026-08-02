package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;

public interface ActStrategy {
    void act(Plant user, GameSession session);
}
