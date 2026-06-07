package model.board.terrain;
public class ShallowCoastTile extends Tile {
    @Override
    public boolean allowsPlant() {
        return false;
    }
}