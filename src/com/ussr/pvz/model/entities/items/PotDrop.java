package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;

public class PotDrop extends GroundItem {

    public PotDrop() {
        super(ItemType.POT,40f,20f);
    }

    @Override
    public void applyRewards(GameSession session, Account account) {
        account.getGreenhouse().unlockPot();
        this.isAlive = false;
        this.setCollected(true);
    }
}
