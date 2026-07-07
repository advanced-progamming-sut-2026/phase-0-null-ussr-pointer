package com.ussr.pvz.model.level.environment;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BigWaveBeachEnvironment implements Environment {

    public record TideEvent(double triggerTimeSeconds, int targetColumn) {}

    private final List<TideEvent> tideSchedule;
    private int currentTideColumn = 9; // Default no water
    private int nextEventIndex = 0;

    public BigWaveBeachEnvironment(int startingTideColumn, List<TideEvent> tideSchedule) {
        this.currentTideColumn = startingTideColumn;
        this.tideSchedule = tideSchedule != null ? new ArrayList<>(tideSchedule) : new ArrayList<>();
        this.tideSchedule.sort(Comparator.comparingDouble(TideEvent::triggerTimeSeconds));
    }

    @Override
    public void onStart(GameSession session) {
        applyTide(session, currentTideColumn);
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        if (nextEventIndex < tideSchedule.size()) {
            TideEvent nextEvent = tideSchedule.get(nextEventIndex);
            if (session.getElapsedSeconds() >= nextEvent.triggerTimeSeconds()) {
                applyTide(session, nextEvent.targetColumn());
                nextEventIndex++;
            }
        }
    }

    private void applyTide(GameSession session, int newTideCol) {
        if (session.getLawn() == null) return;
        this.currentTideColumn = newTideCol;

        int rows = session.getLawn().getRows();
        int cols = session.getLawn().getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = session.getLawn().getCell(r, c);
                if (cell == null) continue;

                if (c >= currentTideColumn) {
                    // This cell is now flooded
                    Plant plant = cell.getPlant();
                    if (plant != null && plant.isAlive()) {
                        // If it's not aquatic and not planted on a Lily Pad (bottom)
                        if (!plant.getTags().contains(Tag.WATER) && plant.getBottom() == null) {
                            plant.setAlive(false);
                            session.notifyPlantDied(plant);
                        }
                    }
                }
            }
        }
    }
}