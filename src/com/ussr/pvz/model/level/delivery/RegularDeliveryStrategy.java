package com.ussr.pvz.model.level.delivery;

import java.util.ArrayList;
import java.util.List;

public class RegularDeliveryStrategy implements DeliveryStrategy {
    private List<String> availablePlants;

    @Override
    public void deliver() {
        // Regular levels require manual sun purchase; no auto-delivery happens here.
    }

    @Override
    public void onLevelStart() {
        // Prepare the standard seed chooser or starting sun if handled here
    }

    @Override
    public List<String> getAvailablePlants(List<String> chapterPlants) {
        this.availablePlants = new ArrayList<>(chapterPlants);
        return this.availablePlants;
    }
}