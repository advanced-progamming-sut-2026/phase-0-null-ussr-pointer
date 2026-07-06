package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;

public class SeedPackDrop extends GroundItem{
    protected SeedPackDrop(ItemType itemType, double lifetime, double collectRadius) {
        super(itemType, lifetime, collectRadius);
    }

    @Override
    public void applyRewards(GameSession session, Account account) {

    }
}
