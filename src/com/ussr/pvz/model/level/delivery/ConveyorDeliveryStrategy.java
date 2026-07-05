package com.ussr.pvz.model.level.delivery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConveyorDeliveryStrategy implements DeliveryStrategy {
    private List<String> availablePlants;
    private final List<String> conveyorBelt = new ArrayList<>();
    private long lastDeliveryTime = 0;
    private final Random random = new Random();
    private static final long DELIVERY_INTERVAL_MS = 12000; // 12 seconds

    @Override
    public void deliver() {
        if (availablePlants == null || availablePlants.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastDeliveryTime >= DELIVERY_INTERVAL_MS) {
            String randomPlant = availablePlants.get(random.nextInt(availablePlants.size()));
            conveyorBelt.add(randomPlant);
            lastDeliveryTime = currentTime;
        }
    }

    @Override
    public void onLevelStart() {
        if (availablePlants == null || availablePlants.isEmpty()) return;

        String randomPlant = availablePlants.get(random.nextInt(availablePlants.size()));
        conveyorBelt.add(randomPlant);
        lastDeliveryTime = System.currentTimeMillis();
    }

    @Override
    public List<String> getAvailablePlants(List<String> chapterPlants) {
        this.availablePlants = new ArrayList<>(chapterPlants);
        return this.availablePlants;
    }

    public List<String> getConveyorBelt() {
        return conveyorBelt;
    }
}