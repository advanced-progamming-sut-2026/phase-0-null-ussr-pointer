package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnClones implements PlantFoodEffect {
    private final int cloneCount;
    private final boolean waterOnly;

    public SpawnClones(int cloneCount, boolean waterOnly) {
        this.cloneCount = cloneCount;
        this.waterOnly = waterOnly;
    }

    public SpawnClones(int cloneCount) {
        this(cloneCount, false);
    }

    public static SpawnClones forLilyPad(int cloneCount) {
        return new SpawnClones(cloneCount, true);
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (user == null || session == null) return;

        System.out.println("spawn clones");

        armPlant(user);

        List<Vec2> validPositions = getValidSpawnPositions(user, session);
        if (validPositions.isEmpty()) return;

        Collections.shuffle(validPositions);

        int spawned = 0;
        for (Vec2 pos : validPositions) {
            if (spawned >= this.cloneCount) break;

            Plant clone = PlantFactory.createPlant(user.getId() , user.getLevel());
            clone.setPosition(pos);
            armPlant(clone);

            session.getLawn().getCell((int) pos.y() , (int) pos.x()).setPlant(clone);
            session.addPlant(clone);
            spawned++;
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        // Instant superpower trigger; no continuous stat modifiers
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        // Instant superpower trigger; no continuous tick handling needed
    }

    private List<Vec2> getValidSpawnPositions(Plant user, GameSession session) {
        List<Vec2> validPositions = new ArrayList<>();

        int minX = 0, maxX = 8;
        int minY = 0, maxY = 4;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (!isCellOccupied(session, x, y)) {
                    if (this.waterOnly && !isWaterTile(session, x , y)) {
                        continue;
                    }
                    validPositions.add(new Vec2(x, y));
                }
            }
        }
        return validPositions;
    }

    private boolean isWaterTile(GameSession session, int col , int lane) {
        return session.getLawn().getTile(lane , col).getType() == TileType.Water;
    }

    private boolean isCellOccupied(GameSession session, int x, int y) {
        if (session.getPlants() == null) return false;

        for (Plant p : session.getPlants()) {
            if (p != null && p.isAlive()) {
                Plant.Location loc = p.getLocation();
                if (loc != null && loc.x() - x < 0.2 && loc.y() - y < 0.2) {
                    // For Lily Pad (waterOnly), a cell is occupied if another Lily Pad / Base plant is present
                    if (this.waterOnly) {
                        if (p.getTags().contains(Tag.WATER) || p.getName().equalsIgnoreCase("Lily Pad")) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void armPlant(Plant plant) {
        plant.setState(Plant.PlantState.ACTIVE);
        plant.setInternalTimer(0.0);
        plant.instantlyMature();
    }
}