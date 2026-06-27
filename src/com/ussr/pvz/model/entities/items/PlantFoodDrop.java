package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;

public class PlantFoodDrop extends GroundItem {
    private final int amount;

    public PlantFoodDrop(int amount) {
        super(ItemType.COIN,40f,20f);
        this.amount = amount;
    }

    @Override
    public void applyRewards(GameSession session, Account account) {
        account.getAdventureProgress().addPlantFood(amount);
        this.isAlive = false;
        this.setCollected(true);
    }
}
