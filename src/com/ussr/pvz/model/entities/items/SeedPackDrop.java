package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;

public class SeedPackDrop extends GroundItem{
    public String type;
    public SeedPackDrop(ItemType itemType, double lifetime, double collectRadius) {
        super(itemType, lifetime, collectRadius);
        //todo randomly find one
    }

    @Override
    public void applyRewards(GameSession session, Account account) {
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {}

}
