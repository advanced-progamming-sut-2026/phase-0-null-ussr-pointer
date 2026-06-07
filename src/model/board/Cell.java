package model.board;

import model.board.structures.InteractableStructure;
import model.board.terrain.Tile;
import model.entities.items.GroundItem;
import model.entities.plants.BasePlant;
import model.entities.zombies.Zombie;

import java.util.List;

public class Cell {
    private int row;
    private int col;
    private Tile tile;
    private BasePlant plant;
    private InteractableStructure interactableStructure;

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Tile getTile() {
        return tile;
    }

    public BasePlant getPlant() {
        return plant;
    }

    public InteractableStructure getInteractableStructure() {
        return interactableStructure;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public void setPlant(BasePlant plant) {
        this.plant = plant;
    }


    public void setStructure(InteractableStructure interactableStructure) {
        this.interactableStructure = interactableStructure;
    }
    public boolean isEmpty() {return plant == null;}
}

