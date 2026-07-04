package com.ussr.pvz.model.board.terrain;

public enum TileType {
    Crater(false),
    Frozen(false),
    Grave(false),
    Necromancy(false),
    Normal(true),
    ShallowCoast(false),
    Slippery(false),
    Water(false),
    Beghouled(true);

    private final boolean allowPlant;

    TileType(boolean allowPlant) {
        this.allowPlant = allowPlant;
    }

    public boolean isAllowPlant() {
        return allowPlant;
    }
}