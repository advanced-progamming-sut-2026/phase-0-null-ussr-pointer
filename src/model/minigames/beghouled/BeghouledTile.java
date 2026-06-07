package model.minigames.beghouled;

import model.entities.plants.BasePlant;

public class BeghouledTile extends model.board.terrain.Tile {
    private BasePlant plant;
    private boolean matched;
    private boolean crater;

    @Override
    public boolean allowsPlant() {
        return false;
    }
}
