package com.ussr.pvz.model.board.terrain;

public class Tile {

    public enum SlipperyDirection { UP, DOWN }

    private TileType type;
    private final SlipperyDirection slipperyDirection;

    public Tile(TileType type) {
        this(type, null);
    }

    public Tile(TileType type, SlipperyDirection slipperyDirection) {
        this.type = type;
        this.slipperyDirection = slipperyDirection;
    }

    public boolean allowsPlant() {
        return this.type.isAllowPlant();
    }

    public TileType getType() {
        return this.type;
    }

    public void setType(TileType type) { this.type = type; }

    public SlipperyDirection getSlipperyDirection() {
        return slipperyDirection;
    }
}