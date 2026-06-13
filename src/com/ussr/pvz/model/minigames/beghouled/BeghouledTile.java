package com.ussr.pvz.model.minigames.beghouled;

import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.entities.plants.BasePlant;

public class BeghouledTile extends Tile {
    private BasePlant plant;
    private boolean matched;
    private boolean crater;

    @Override
    public boolean allowsPlant() {
        return false;
    }
}
