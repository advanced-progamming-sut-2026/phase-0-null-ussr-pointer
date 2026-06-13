package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;

public abstract class InteractableStructure extends GameEntity {
    private boolean destroyed;
    private int hp;

    public void takeDamage(int damage) {}
    public abstract void onDestroy(GameSession session);
    @Override public void tick() {}

}

