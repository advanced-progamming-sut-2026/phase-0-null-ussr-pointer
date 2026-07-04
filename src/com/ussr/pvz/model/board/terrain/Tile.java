package com.ussr.pvz.model.board.terrain;

public class Tile {
    private final TileType type;

    public Tile(TileType type) {
        this.type = type;
    }

    public boolean allowsPlant() {
        return this.type.isAllowPlant();
    }

    public TileType getType() {
        return this.type;
    }
}