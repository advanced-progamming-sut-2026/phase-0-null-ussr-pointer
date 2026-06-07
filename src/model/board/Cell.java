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

    public BasePlant getPlant() {
        return null;
    }
    public List<Zombie> getZombies() {
        return List.of();
    }
    public InteractableStructure getStructure() {
        return null;
    }
    public List<GroundItem> getItems() {
        return List.of();
    }
    public Tile getTile() {return this.tile;}
    public boolean isEmpty() {
        return false;
    }
}

