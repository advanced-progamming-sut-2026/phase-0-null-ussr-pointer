package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class Vase extends InteractableStructure {
    private VaseType type;
    private Plant containedPlant;
    private Zombie containedZombie;

    @Override
    public void onDestroy(GameSession session) {

    }

    @Override
    public void tick() {
    }

}

