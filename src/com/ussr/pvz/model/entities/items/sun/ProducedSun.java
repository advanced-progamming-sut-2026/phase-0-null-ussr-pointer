package com.ussr.pvz.model.entities.items.sun;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;

public class ProducedSun extends GroundItem {
    private final int x;
    private final int y;
    private final int value;

    public ProducedSun(int x, int y, int value, String sourcePlantName) {
        super(ItemType.SUN, 40f, 20f);
        this.x = x;
        this.y = y;
        this.value = value;
        this.setPosition(com.ussr.pvz.model.util.Vec2.of(x, y));
        App.getGameSession().getEventBus().publish(new GameEvent.SunProduced(sourcePlantName, value, x, y));
    }

    @Override
    public void applyRewards(GameSession session, Account account) {
        session.addSun(value);
        this.isAlive = false;
        this.setCollected(true);
        App.getGameSession().getEventBus().publish(new GameEvent.SunCollected(value, App.getGameSession()
                .getSunCount()));
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