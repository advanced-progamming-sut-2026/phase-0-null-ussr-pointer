package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;

public interface MoveBehavior {
    void move(Zombie zombie, GameSession session);
}
