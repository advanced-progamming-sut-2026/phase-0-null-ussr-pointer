package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.level.Level;

import java.util.List;

public class BigWaveBeachEffect implements ChapterEffect {

    @Override
    public void onStart(GameSession session, Level level) {
        applyTide(session, level, level.getStartingTideColumn());
    }

    @Override
    public void onTick(GameSession session, Level level, double deltaTime) {
        List<Level.TideEvent> schedule = level.getTideSchedule();
        int nextIndex = level.getNextTideIndex();

        if (nextIndex >= schedule.size()) return;

        Level.TideEvent nextEvent = schedule.get(nextIndex);
        if (session.getElapsedSeconds() >= nextEvent.triggerTimeSeconds()) {
            applyTide(session, level, nextEvent.targetColumn());
            level.setNextTideIndex(nextIndex + 1);
        }
    }

    private void applyTide(GameSession session, Level level, int newTideCol) {
        Lawn lawn = session.getLawn();
        if (lawn == null) return;

        level.setCurrentTideColumn(newTideCol);
        System.out.println("The tide shifts: water now covers columns " + (newTideCol + 1) + " onward.");

        int rows = lawn.getRows();
        int cols = lawn.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = lawn.getCell(r, c);
                if (cell == null || cell.getTile() == null) continue;

                TileType current = cell.getTile().getType();
                boolean shouldBeWater = c >= newTideCol && current == TileType.Normal;
                boolean shouldBeLand = c < newTideCol
                        && (current == TileType.Water || current == TileType.ShallowCoast);

                if (shouldBeWater) {
                    cell.setTile(new Tile(TileType.Water));
                    washAwayIfNeeded(session, cell);
                } else if (shouldBeLand) {
                    cell.setTile(new Tile(TileType.Normal));
                }
            }
        }
    }

    private void washAwayIfNeeded(GameSession session, Cell cell) {
        Plant plant = cell.getPlant();
        if (plant == null || !plant.isAlive()) return;
        if (plant.getTags().contains(Tag.WATER)) return; // water-safe plants stay put
        if (plant.getBottom() != null) return; // planted on a lily pad / raft, stays afloat

        System.out.println(plant.getName() + " was swept away by the tide!");
        plant.setAlive(false);
        session.notifyPlantDied(plant);
    }
}