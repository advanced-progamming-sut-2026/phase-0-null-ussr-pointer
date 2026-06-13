package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.GameSession;

public class Grave extends InteractableStructure {
    private String zombieId;
    private boolean cleared;

    @Override
    public void onDestroy(GameSession session) {

    }

    @Override public void tick() {}

}