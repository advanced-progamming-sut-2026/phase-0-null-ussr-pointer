package com.ussr.pvz.model.entities.items;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;

public class SeedPackDrop extends GroundItem {
    private final int plantId;

    public SeedPackDrop(ItemType itemType, double lifetime, double collectRadius, int plantId) {
        super(itemType, lifetime, collectRadius);
        this.plantId = plantId;
    }

    @Override
    public void applyRewards(GameSession session, Account account) {
        // Handled by VaseBreakerService directly when planted
    }

    public int getPlantId() {
        return plantId;
    }
}