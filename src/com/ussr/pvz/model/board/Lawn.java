package com.ussr.pvz.model.board;

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

    public int getRows() { return numRows; }
    public int getCols() { return numCols; }
}