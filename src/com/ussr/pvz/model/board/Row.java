package com.ussr.pvz.model.board;

import java.util.ArrayList;
import java.util.List;

public class Row {
    private final int index;
    private final List<Cell> cells = new ArrayList<>();

    public Row(int index) {
        this.index = index;
    }

    public void addCell(Cell cell) {
        cells.add(cell);
    }

    public int getIndex() { return index; }

    public Cell getCell(int col) {
        if (col < 0 || col >= cells.size()) return null;
        return cells.get(col);
    }

    public List<Cell> getCells() { return cells; }
}