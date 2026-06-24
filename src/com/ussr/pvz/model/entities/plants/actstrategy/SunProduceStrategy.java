package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.plants.Plant;

public class SunProduceStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        int x = user.getLocation().x();
        int y = user.getLocation().y();
        session.getItems().forEach(item -> {
            if (item.getItemType().equals(ItemType.SUN) && !item.isCollected() && item.getLocation().equals(new GroundItem.Location(x, y))) {
                return;
            }
            // todo currently the damage is count as the value implement this or refactor when the plant changed
            ProducedSun sun = new ProducedSun(user.getLocation().x(), user.getLocation().y(), user.getDamage());
            session.getItems().add(sun);
        });
    }
}
