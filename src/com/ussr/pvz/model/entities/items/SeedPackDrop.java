package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;

public class SeedPackDrop extends GroundItem{
    public SeedPackDrop(ItemType itemType, double lifetime, double collectRadius) {
        super(itemType, lifetime, collectRadius);
    }

    @Override
    public void applyRewards(GameSession session, Account account) {
        //todo implement this after completed the plant factory
        // make a plant and put in lawn
    }

    public void applyReward(GameSession session, Account account, int row , int column) {
        //todo implement this after completed the plant factory
        // make a plant and put in lawn
    }
}
