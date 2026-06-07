package model.board.terrain;
public class NecromancyTile extends Tile {
    @Override
    public boolean allowsPlant() {
        return false;
    }
}