package com.ussr.pvz.model.engine.session;

import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.event.GameEventBus;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public final class SessionEventPublisher {
    private final GameEventBus eventBus;

    public SessionEventPublisher(
            GameEventBus eventBus
    ) {
        this.eventBus = eventBus;
    }

    public void plantFoodUsed(Plant plant) {
        eventBus.publish(new GameEvent.PlantFoodUsed(
                plant.getName(),
                plant.getLocation().y(),
                plant.getLocation().x()
        ));
    }

    public void plantPlanted(Plant plant) {
        eventBus.publish(new GameEvent.PlantPlanted(
                plant.getName(),
                plant.getLocation().y(),
                plant.getLocation().x()
        ));
    }

    public void plantPlucked(Plant plant) {
        eventBus.publish(new GameEvent.PlantPlucked(
                plant.getName(),
                plant.getLocation().y(),
                plant.getLocation().x()
        ));
    }

    public void zombieDied(
            Zombie zombie,
            String killerName
    ) {
        eventBus.publish(new GameEvent.ZombieDied(
                zombie.getAlias(),
                zombie.getPosition().x(),
                zombie.getPosition().y(),
                killerName
        ));
    }

    public void graveDestroyed(int row, int column) {
        eventBus.publish(
                new GameEvent.GraveDestroyed(row, column)
        );
    }

    public void structureDestroyed(
            String type,
            int row,
            int column
    ) {
        eventBus.publish(
                new GameEvent.StructureDestroyed(
                        type,
                        row,
                        column
                )
        );
    }
}