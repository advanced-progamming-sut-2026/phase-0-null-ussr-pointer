package com.ussr.pvz.model.entities.items.sun;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;

public class ProducedSun extends GroundItem {
    private final int x;
    private final int y;
    private final int value;

    public ProducedSun(int x, int y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    @Override
    public void applyRewards(GameSession session, Account account) {
        session.addSun(value);
        this.isAlive = false;
    }

    public int getValue() {
        return value;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

}
