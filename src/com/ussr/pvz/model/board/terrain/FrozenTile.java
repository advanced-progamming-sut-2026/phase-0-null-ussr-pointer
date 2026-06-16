package com.ussr.pvz.model.board.terrain;

public class FrozenTile extends Tile {
    @Override
    public boolean allowsPlant() {
        return false;
    }
}