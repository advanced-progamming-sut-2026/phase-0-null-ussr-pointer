package com.ussr.pvz.model.level.delivery;

import com.ussr.pvz.model.engine.GameSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConveyorDeliveryStrategy implements DeliveryStrategy {

    private List<String> availablePlants;
    private final List<String> conveyorBelt = new ArrayList<>();
    private final Random random = new Random();

    @Override
    public void deliver() {
        if (availablePlants == null || availablePlants.isEmpty()) return;

        String randomPlant = availablePlants.get(random.nextInt(availablePlants.size()));
        conveyorBelt.add(randomPlant);
    }

    @Override
    public void onLevelStart() {
        if (availablePlants == null || availablePlants.isEmpty()) return;
        deliver();
    }

    @Override
    public List<String> getAvailablePlants(List<String> chapterPlants) {
        this.availablePlants = new ArrayList<>(chapterPlants);
        return this.availablePlants;
    }

    public List<String> getConveyorBelt() {
        return conveyorBelt;
    }

    public void setAvailablePlants(List<String> availablePlants) {
        this.availablePlants = availablePlants;
    }
}