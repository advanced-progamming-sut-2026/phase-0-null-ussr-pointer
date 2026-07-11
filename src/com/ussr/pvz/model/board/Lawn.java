package com.ussr.pvz.model.board;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.board.terrain.Tile;

import java.util.ArrayList;
import java.util.List;

public class Lawn {
    private final List<Row> rows = new ArrayList<>();
    private final int numRows;
    private final int numCols;

    public Lawn(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public void addRow(Row row) {
        rows.add(row);
    }

    public Row getRow(int r) {
        if (r < 0 || r >= rows.size()) return null;
        return rows.get(r);
    }

    public Cell getCell(int r, int c) {
        Row row = getRow(r);
        if (row == null) return null;
        return row.getCell(c);
    }

    public Tile getTile(int row , int col) {
        if(row < 1 || row > 5 || col < 1 || col > 9) return  null;
        return rows.get(row-1).getCell(col - 1).getTile();
    }

    public int getRows() { return numRows; }
    public int getCols() { return numCols; }

    public ArrayList<InteractableStructure> getAllInteractable() {
        ArrayList<InteractableStructure> allInteractable = new ArrayList<>();
        for(Row row : rows) {

            for(Cell cell : row.getCells()) {

                InteractableStructure structure = cell.getInteractableStructure();
                if(structure != null)
                    allInteractable.add(structure);
            }

        }
        return allInteractable;
    }
}