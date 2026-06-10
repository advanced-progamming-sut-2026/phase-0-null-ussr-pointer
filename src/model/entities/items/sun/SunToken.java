package model.entities.items.sun;

import model.engine.GameSession;
import model.entities.items.GroundItem;
import model.entities.items.ItemType;

public class SunToken extends GroundItem {
    private final SunDropType dropType;
    private final int sunValue;
    private boolean falling;
    private double fallTargetY;
    private long fallTime;

    public SunToken(SunDropType dropType, int sunValue) {
        this.dropType = dropType;
        this.sunValue = sunValue;
    }


    @Override
    public void applyRewards(GameSession session) {

    }

    @Override
    public void tick() {}


}
