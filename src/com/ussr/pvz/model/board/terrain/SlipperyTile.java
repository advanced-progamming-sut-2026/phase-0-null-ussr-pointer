package com.ussr.pvz.model.board.terrain;
public class SlipperyTile extends Tile {
    @Override
    public boolean allowsPlant() {
        return false;
    }
}