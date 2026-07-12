package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;

public abstract class GroundItem extends GameEntity {
    private final double lifetime;
    private final double collectRadius;
    private boolean collected;
    private final ItemType itemType;

    protected GroundItem(ItemType itemType, double lifetime, double collectRadius) {
        this.itemType = itemType;
        this.collected = false;
        this.collectRadius = collectRadius;
        this.lifetime = lifetime;
    }

    public void collect() {
        applyRewards(App.getGameSession(), App.getAccount());
    }

    public boolean isExpired() {
        return false;
    }

    public abstract void applyRewards(GameSession session, Account account);

    @Override
    public void tick() {
    }

    public ItemType getItemType() {
        return itemType;
    }

    public double getLifetime() {
        return lifetime;
    }

    public double getCollectRadius() {
        return collectRadius;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public Location getLocation() {
        if (this.getPosition() == null) return null;
        return new Location((int) this.getPosition().x(), (int) this.getPosition().y());
    }

    public record Location(int x, int y) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Location(int x1, int y1))) return false;
            return x == x1 && y == y1;
        }
    }
}
