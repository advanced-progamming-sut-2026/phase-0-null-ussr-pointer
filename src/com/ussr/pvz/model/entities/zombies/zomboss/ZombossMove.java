package com.ussr.pvz.model.entities.zombies.zomboss;

import com.ussr.pvz.model.engine.session.GameSession;

import java.util.List;

public interface ZombossMove {
    void execute(ZombossController controller, GameSession session);

    default void execute(ZombossController controller, GameSession session, List<String> playingClips) {
        execute(controller, session);
    }
}