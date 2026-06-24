package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;

public class PotDrop extends GroundItem {

    @Override
    public void applyRewards(GameSession session, Account account) {
        account.getGreenhouse().unlockPot();
    }
}
