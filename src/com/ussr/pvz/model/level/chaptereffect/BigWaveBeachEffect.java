package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.terrain.Tile;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.level.Level;

import java.util.List;

public class BigWaveBeachEffect implements ChapterEffect {
    public static final int WATER_LIMIT_COLUMN = 2;

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
            session.getEventBus().publish(
                    new GameEvent.SpecialLevelAnnouncement(
                            "THE TIDE IS CHANGING!"
                    )
            );
            applyTide(session, level, nextEvent.targetColumn());
            level.setNextTideIndex(nextIndex + 1);
        }
    }

    private void applyTide(
            GameSession session,
            Level level,
            int requestedCoastColumn
    ) {
        Lawn lawn = session.getLawn();
        if (lawn == null) {
            return;
        }

        int rows = lawn.getRows();
        int cols = lawn.getCols();

        int coastColumn = Math.max(
                WATER_LIMIT_COLUMN,
                Math.min(requestedCoastColumn, cols - 2)
        );

        level.setCurrentTideColumn(coastColumn);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = lawn.getCell(row, col);
                if (cell == null || cell.getTile() == null) {
                    continue;
                }

                TileType oldType = cell.getTile().getType();
                TileType newType = getBeachTileType(col, coastColumn);

                if (!isBeachTerrain(oldType)) {
                    continue;
                }

                if (oldType != newType) {
                    cell.setTile(new Tile(newType));

                    if (isNewlyFlooded(oldType, newType)) {
                        washAwayIfNeeded(session, cell);
                    }
                }
            }
        }
    }

    private TileType getBeachTileType(
            int column,
            int coastColumn
    ) {
        if (column < coastColumn) {
            return TileType.Normal;
        }

        if (column == coastColumn) {
            return TileType.ShallowCoast;
        }

        return TileType.Water;
    }

    private boolean isBeachTerrain(TileType type) {
        return type == TileType.Normal
                || type == TileType.ShallowCoast
                || type == TileType.Water;
    }

    private boolean isNewlyFlooded(
            TileType oldType,
            TileType newType
    ) {
        return oldType == TileType.Normal
                && (newType == TileType.ShallowCoast
                || newType == TileType.Water);
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
