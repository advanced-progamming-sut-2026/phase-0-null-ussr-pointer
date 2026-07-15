package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;

public class SunProduceStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        int x = user.getLocation().x();
        int y = user.getLocation().y();

        boolean sunAlreadyExists = session.getItems().stream()
                .anyMatch(item -> item.getItemType() == ItemType.SUN
                        && !item.isCollected()
                        && item.getLocation().x() == x
                        && item.getLocation().y() == y);

        if (!sunAlreadyExists) {
            if(user.getIntervalTimer() <= 0) {
                // This naturally grabs the current stage's sun value thanks to Plant.java's getter
                int sunValue = (int) user.getAbilityValue();

                ProducedSun sun = new ProducedSun(x, y, sunValue, user.getName());
                session.addItem(sun);
                user.setInternalTimer(user.getActionInterval());
            }
        }
    }
}