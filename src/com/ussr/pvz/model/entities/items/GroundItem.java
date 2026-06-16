package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;

public abstract class GroundItem extends GameEntity {
    private double lifetime;
    private double collectRadius;
    private boolean collected;

    public void collect() {
    }

    public boolean isExpired() {
        return false;
    }

    public abstract void applyRewards(GameSession session);

    @Override
    public void tick() {
    }


}
