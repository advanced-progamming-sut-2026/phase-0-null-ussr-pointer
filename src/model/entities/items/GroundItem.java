package model.entities.items;

import model.account.Account;
import model.engine.GameEntity;
import model.engine.GameSession;

public abstract  class GroundItem extends GameEntity {
    private double lifetime;
    private double collectRadius;

    public void collect() {}
    public boolean isExpired() {
        return false;
    }
    public abstract void applyRewards(GameSession session);
    @Override public void tick() {}



}
