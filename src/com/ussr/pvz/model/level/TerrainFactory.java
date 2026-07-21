package com.ussr.pvz.model.level;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.Row;
import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.util.Vec2;

import java.util.Random;

public final class TerrainFactory {

    private static final Random RAND = new Random();

    private TerrainFactory() {
    }

    public static Lawn build(String chapterId, int rows, int cols) {
        Lawn lawn = new Lawn(rows, cols);

        for (int r = 0; r < rows; r++) {
            Row row = new Row(r);
            for (int c = 0; c < cols; c++) {
                Cell cell = new Cell();
                cell.setRow(r);
                cell.setCol(c);
                cell.setTile(new Tile(TileType.Normal));
                row.addCell(cell);
            }
            lawn.addRow(row);
        }

        if (chapterId == null) return lawn;

        switch (chapterId) {
            case "ancient_egypt" -> scatterGraves(lawn, rows, cols);
            case "frostbite_caves" -> scatterFrostbiteTerrain(lawn, rows, cols);
            case "big_wave_beach" -> scatterBeachTerrain(lawn, rows, cols);
            case "dark_ages" -> scatterDarkAgesTerrain(lawn, rows, cols);
            default -> {}
        }

        return lawn;
    }

    private static void scatterGraves(Lawn lawn, int rows, int cols) {
        int count = 3 + RAND.nextInt(3);
        for (int i = 0; i < count; i++) {
            int r = RAND.nextInt(rows);
            int c = RAND.nextInt(cols);
            lawn.getCell(r, c).setTile(new Tile(TileType.Grave));
            Grave grave = new Grave();
            grave.setPosition(Vec2.of(c, r));
            lawn.getCell(r, c).setStructure(grave);
        }
    }

    private static void scatterFrostbiteTerrain(Lawn lawn, int rows, int cols) {
        int slipperyCount = 2 + RAND.nextInt(3);
        for (int i = 0; i < slipperyCount; i++) {
            int r = RAND.nextInt(rows);
            int c = RAND.nextInt(cols);
            Tile.SlipperyDirection dir = RAND.nextBoolean()
                    ? Tile.SlipperyDirection.UP
                    : Tile.SlipperyDirection.DOWN;
            lawn.getCell(r, c).setTile(new Tile(TileType.Slippery, dir));
        }
    }

    private static void scatterBeachTerrain(Lawn lawn, int rows, int cols) {
        int seaColumns = 3;
        for (int r = 0; r < rows; r++) {
            for (int c = cols - seaColumns; c < cols; c++) {
                TileType type = RAND.nextInt(4) == 0 ? TileType.ShallowCoast : TileType.Water;
                lawn.getCell(r, c).setTile(new Tile(type));
            }
        }
    }

    private static void scatterDarkAgesTerrain(Lawn lawn, int rows, int cols) {
        int graveCount = 2 + RAND.nextInt(3);
        for (int i = 0; i < graveCount; i++) {
            int r = RAND.nextInt(rows);
            int c = RAND.nextInt(cols);
            boolean necromancy = RAND.nextInt(3) == 0;
            lawn.getCell(r, c).setTile(new Tile(necromancy ? TileType.Necromancy : TileType.Grave));
        }
    }
}