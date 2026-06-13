package com.ussr.pvz.model.board.terrain;
public class GraveTile extends Tile {
    @Override
    public boolean allowsPlant() {
        return false;
    }
}