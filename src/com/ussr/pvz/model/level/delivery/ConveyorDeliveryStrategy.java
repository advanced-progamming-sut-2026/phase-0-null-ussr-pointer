package com.ussr.pvz.model.level.delivery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConveyorDeliveryStrategy implements DeliveryStrategy {
    private static final int MAX_CAPACITY = 6;

    private List<String> availablePlants;
    private final List<String> conveyorBelt = new ArrayList<>();
    private final Random random = new Random();

    @Override
    public void deliver() {
        if (availablePlants == null
                || availablePlants.isEmpty()
                || conveyorBelt.size() >= MAX_CAPACITY) {
            return;
        }

        String randomPlant = availablePlants.get(
                random.nextInt(availablePlants.size())
        );
        conveyorBelt.add(randomPlant);
    }

    @Override
    public void onLevelStart() {
        deliver();
    }

    @Override
    public List<String> getAvailablePlants(List<String> chapterPlants) {
        this.availablePlants = chapterPlants == null
                ? new ArrayList<>()
                : new ArrayList<>(chapterPlants);
        return new ArrayList<>(availablePlants);
    }

    public List<String> getConveyorBelt() {
        return conveyorBelt;
    }

    public void setAvailablePlants(List<String> availablePlants) {
        this.availablePlants = availablePlants == null
                ? new ArrayList<>()
                : new ArrayList<>(availablePlants);
    }

    public boolean contains(String plantName) {
        String wanted = normalize(plantName);
        return conveyorBelt.stream()
                .anyMatch(name -> normalize(name).equals(wanted));
    }

    public boolean consume(String plantName) {
        String wanted = normalize(plantName);
        return conveyorBelt.stream()
                .filter(name -> normalize(name).equals(wanted))
                .findFirst()
                .map(conveyorBelt::remove)
                .orElse(false);
    }

    private String normalize(String name) {
        return name == null
                ? ""
                : name.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "");
    }
}
