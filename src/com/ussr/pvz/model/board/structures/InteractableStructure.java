package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;

public abstract class InteractableStructure extends GameEntity implements Damageable {

    public abstract void onDestroy(GameSession session);

    @Override
    public void tick() {
    }

    public abstract void takeDamage(int damage);

}