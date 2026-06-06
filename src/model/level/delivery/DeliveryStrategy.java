package model.level.delivery;

import java.util.List;

public interface DeliveryStrategy {
    void         deliver();
    void         onLevelStart();
    List<String> getAvailablePlants(List<String> chapterPlants);
}
