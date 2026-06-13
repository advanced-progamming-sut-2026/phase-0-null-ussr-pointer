package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.engine.GameSession;

public class CoinDrop extends GroundItem {
    public CoinDrop(CoinTier tier) {
        this.tier = tier;
    }

    public enum CoinTier {
        BRONZE,
        SILVER,
        GOLD;
    }
    private final CoinTier tier;

    @Override
    public void applyRewards(GameSession session) {

    }
}
