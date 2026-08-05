package com.ussr.pvz.model.entities.zombies.zomboss;

import com.ussr.pvz.model.engine.session.GameSession;

public interface ZombossMove {
    void execute(ZombossController controller, GameSession session);
}