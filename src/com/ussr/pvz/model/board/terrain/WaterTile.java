package com.ussr.pvz.model.board.terrain;

public class WaterTile extends Tile {
    @Override
    public boolean allowsPlant() {
        return false;
    }
}