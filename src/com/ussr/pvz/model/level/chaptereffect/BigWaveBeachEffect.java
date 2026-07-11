package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.level.Level;

import java.util.Random;

/**
 * Rising/falling tide for Big Wave Beach levels: the number of sea columns
 * on the right side of the lawn changes every wave. Plants that cannot
 * survive in water (no WATER tag) are washed away when the tide reaches
 * them.
 *
 * NOTE: if you model lily pads as a structure rather than a Tag, add a
 * check for that structure in washAwayIfNeeded() so plants sitting on a
 * lily pad survive the tide.
 */
public class BigWaveBeachEffect implements ChapterEffect {

    private static final Random RAND = new Random();
    private static final int MIN_SEA_COLUMNS = 3;
    private static final int MAX_SEA_COLUMNS = 5;

    @Override
    public void onWaveStart(GameSession session, Level level, int waveNumber, boolean isFinalWave) {
        Lawn lawn = session.getLawn();
        if (lawn == null) return;

        int cols = lawn.getCols();
        int rows = lawn.getRows();
        int seaColumns = MIN_SEA_COLUMNS + RAND.nextInt(MAX_SEA_COLUMNS - MIN_SEA_COLUMNS + 1);
        int seaStart = cols - seaColumns;

        System.out.println("The tide shifts: the sea now covers the rightmost " + seaColumns + " columns.");

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = lawn.getCell(r, c);
                if (cell == null || cell.getTile() == null) continue;

                TileType current = cell.getTile().getType();
                boolean shouldBeSea = c >= seaStart && current == TileType.Normal;
                boolean shouldBeLand = c < seaStart
                        && (current == TileType.Water || current == TileType.ShallowCoast);

                if (shouldBeSea) {
                    cell.setTile(new Tile(TileType.Water));
                    washAwayIfNeeded(cell);
                } else if (shouldBeLand) {
                    cell.setTile(new Tile(TileType.Normal));
                }
            }
        }
    }

    private void washAwayIfNeeded(Cell cell) {
        Plant plant = cell.getPlant();
        if (plant == null || !plant.isAlive()) return;
        if (plant.getTags().contains(Tag.WATER)) return; // water-safe plants stay put

        System.out.println(plant.getName() + " at (" + cell.getCol() + ", " + cell.getRow() + ") was swept away by the tide!");
        plant.setAlive(false);
        cell.setPlant(null);
    }
}