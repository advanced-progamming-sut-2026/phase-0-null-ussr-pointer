package model.level.delivery;

import java.util.List;

public class CardDeliveryStrategy implements DeliveryStrategy {
    @Override
    public void deliver() {
    }

    @Override
    public void onLevelStart() {
    }

    @Override
    public List<String> getAvailablePlants(List<String> chapterPlants) {
        return chapterPlants;
    }
}

