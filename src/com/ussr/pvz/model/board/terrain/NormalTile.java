package com.ussr.pvz.model.board.terrain;

public class NormalTile extends Tile {
    @Override
    public boolean allowsPlant() {
        return true;
    }
}