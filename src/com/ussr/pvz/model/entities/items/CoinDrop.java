package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;

public class CoinDrop extends GroundItem {
    public CoinDrop(CoinTier tier) {
        super(ItemType.COIN,40f,20f);
        this.tier = tier;
    }

    public enum CoinTier {
        BRONZE,
        SILVER,
        GOLD;
    }

    private final CoinTier tier;

    @Override
    public void applyRewards(GameSession session, Account account) {
        int value = switch (tier) {
            case BRONZE -> 10;
            case SILVER -> 50;
            case GOLD -> 100;
        };
        account.getAdventureProgress().addCoin(value);
        this.isAlive = false;
        this.setCollected(true);
    }
}
