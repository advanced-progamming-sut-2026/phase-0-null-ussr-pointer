package model.board.terrain;
public abstract class Tile {
    private TileType type;

    abstract public boolean allowsPlant();
    public TileType getType() {return this.type;}
}