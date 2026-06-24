package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;

public abstract class GroundItem extends GameEntity {
    private double lifetime;
    private double collectRadius;
    private boolean collected;
    private ItemType itemType;

    public void collect() {
        applyRewards(App.getGameSession(),App.getAccount());
    }

    public boolean isExpired() {
        return false;
    }

    public abstract void applyRewards(GameSession session, Account account);

    @Override
    public void tick() {
    }

}
