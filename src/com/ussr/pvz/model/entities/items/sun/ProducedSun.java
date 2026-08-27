package com.ussr.pvz.model.entities.items.sun;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;

public class ProducedSun extends GroundItem {
    private final int x;
    private final int y;
    private final int value;
    private final float offsetX;
    private final float offsetY;

    private static final float POP_DURATION = 0.7f;

    public static float getPopDurationSeconds() {
        return POP_DURATION;
    }

    private boolean popping = true;
    private float popElapsed = 0f;

    public ProducedSun(int x, int y, int value, String sourcePlantName) {
        this(x, y, value, sourcePlantName, 0f, 0f);
    }

    public ProducedSun(int x, int y, int value, String sourcePlantName, float offsetX, float offsetY) {
        super(ItemType.SUN, 40f, 20f);
        this.x = x;
        this.y = y;
        this.value = value;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.setPosition(com.ussr.pvz.model.util.Vec2.of(x, y));
        App.getGameSession().getEventBus().publish(new GameEvent.SunProduced(sourcePlantName, value, x, y));
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    @Override
    public void update(float delta) {
        if (!popping) return;
        popElapsed += delta;
        if (popElapsed >= POP_DURATION) {
            popElapsed = POP_DURATION;
            popping = false;
        }
    }

    public boolean isPopping() {
        return popping;
    }

    public float getPopProgress() {
        return popElapsed / POP_DURATION;
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