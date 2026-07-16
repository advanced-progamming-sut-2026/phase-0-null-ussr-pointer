package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;

public class SunProduceStrategy implements ActStrategy {
    private boolean isInstantBurst;

    // Default constructor (keeps Sunflower behaving exactly as it did before)
    public SunProduceStrategy() {
        this.isInstantBurst = false;
    }
    @Override
    public void act(Plant user, GameSession session) {
        setInstantBurst(user);
        int x = user.getLocation().x();
        int y = user.getLocation().y();

        boolean sunAlreadyExists = session.getItems().stream()
                .anyMatch(item -> item.getItemType() == ItemType.SUN
                        && !item.isCollected()
                        && item.getLocation().x() == x
                        && item.getLocation().y() == y);

        if (sunAlreadyExists) {
            return;
        }

        int sunValue = (int) user.getAbilityValue();
        ProducedSun sun = null;
        if(isInstantBurst) {
            sun = new ProducedSun(x , y , sunValue , user.getName());
            //todo : we may should make the plant of the sell null when a plant dies ( we can do it somewhere else like in the set alive method)
            user.setAlive(false);
        }
        else {
            sun = new ProducedSun(x, y, sunValue, user.getName());
        }
        session.addItem(sun);

        user.setInternalTimer(0.0);
    }

    private void setInstantBurst(Plant user) {
        if(user.getName().equalsIgnoreCase("gold bloom"))
            isInstantBurst = true;
    }
}