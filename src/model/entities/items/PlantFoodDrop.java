package model.entities.items;

import model.engine.GameSession;

public class PlantFoodDrop extends GroundItem {
    private final int amount;

    public PlantFoodDrop(int amount) {
        this.amount = amount;
    }

    @Override
    public void applyRewards(GameSession session) {

    }
}
