package model.board.structures;

import model.engine.GameSession;
import model.entities.plants.BasePlant;
import model.entities.zombies.Zombie;

public class Vase extends InteractableStructure {
    private VaseType type;
    private BasePlant containedPlant;
    private Zombie containedZombie;

    @Override
    public void onDestroy(GameSession session) {

    }
    @Override public void tick() {}

}

