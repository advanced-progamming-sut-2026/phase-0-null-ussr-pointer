package com.ussr.pvz.model.entities.items.sun;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;

public class SunToken extends GroundItem {
    private final SunDropType dropType;
    private boolean falling;
    private double fallTargetY;
    private long fallTime;

    public SunToken(SunDropType dropType, boolean falling) {
        this.dropType = dropType;
        this.falling = falling;
    }


    @Override
    public void applyRewards(GameSession session) {

    }

    @Override
    public void tick() {
    }


    public SunDropType getDropType() {
        return dropType;
    }

    public int getValue() {
        return dropType.getValue();
    }

    public boolean isFalling() {
        return falling;
    }

    public void setFalling(boolean falling) {
        this.falling = falling;
    }
}
