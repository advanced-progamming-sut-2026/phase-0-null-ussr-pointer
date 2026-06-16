package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.GameSession;

public class Container extends InteractableStructure {
    private int hp;
    private String containedZombieId;

    @Override
    public void onDestroy(GameSession session) {

    }

    @Override
    public void tick() {
    }

}
