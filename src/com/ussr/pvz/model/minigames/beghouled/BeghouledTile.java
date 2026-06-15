package com.ussr.pvz.model.minigames.beghouled;

import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.entities.plants.Plant;

public class BeghouledTile extends Tile {
    private Plant plant;
    private boolean matched;
    private boolean crater;

    @Override
    public boolean allowsPlant() {
        return false;
    }
}
