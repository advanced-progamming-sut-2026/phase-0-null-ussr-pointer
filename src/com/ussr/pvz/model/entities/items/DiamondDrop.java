package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.engine.GameSession;

public class DiamondDrop extends GroundItem {
    private final int amount;

    public DiamondDrop(int amount) {
        this.amount = amount;
    }

    @Override
    public void applyRewards(GameSession session) {

    }
}
