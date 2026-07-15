package com.ussr.pvz.model.level;

import com.ussr.pvz.model.board.terrain.TileType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Chapter {
    private final String id;
    private final String name;
    private final GameMode gameMode;
    private List<String> allowedPlants;
    private final List<Level> levels;
    private List<TileType> allowedTiles;

    public Chapter(String id, String name, GameMode gameMode, List<TileType> allowedTiles) {
        this.id = id;
        this.name = name;
        this.gameMode = gameMode;
        this.allowedTiles = allowedTiles != null ? allowedTiles : new ArrayList<>();
        this.levels = new ArrayList<>();
        this.allowedPlants = new ArrayList<>();
    }

    public void addLevel(Level level) {
        if (level != null) {
            levels.add(level);
        }
    }

    public Optional<Level> findLevel(String id) {
        if (id == null) return Optional.empty();
        return levels.stream().filter(l -> l.getId().equals(id)).findFirst();
    }

    // === Core Setters & JSON Data Binding Bridges ===

    public void setAllowedPlants(List<String> p) {
        this.allowedPlants = p != null ? p : new ArrayList<>();
    }

    /**
     * Bridges the gap between raw JSON data configuration strings and
     * the internal strong-typed TileType enums used by the engine terrain system.
     */
    public void setAllowedTiles(List<String> rawTileNames) {
        if (rawTileNames == null) {
            this.allowedTiles = new ArrayList<>();
            return;
        }

        List<TileType> parsedTypes = new ArrayList<>();
        for (String name : rawTileNames) {
            if (name == null || name.isBlank()) continue;

            String sanitized = name.trim().toUpperCase();
            TileType type = switch (sanitized) {
                case "SAND_TILE", "GRASS_TILE", "NORMAL" -> TileType.Normal;
                case "WATER_TILE", "WATER" -> TileType.Water;
                case "TOMBSTONE_TILE", "GRAVESTONE_TILE", "GRAVE" -> TileType.Grave;
                case "ICE_TILE", "FROZEN" -> TileType.Frozen;
                case "SLIDER_TILE", "SLIPPERY" -> TileType.Slippery;
                case "SHALLOW_COAST", "SHALLOWCOAST" -> TileType.ShallowCoast;
                case "NECROMANCY_TILE", "NECROMANCY" -> TileType.Necromancy;
                case "CRATER_TILE", "CRATER" -> TileType.Crater;
                case "BEGHOULED_TILE", "BEGHOULED" -> TileType.Beghouled;
                default -> null;
            };

            if (type != null) {
                parsedTypes.add(type);
            } else {
                System.err.println("[Chapter Warning] Skipping invalid TileType entry configured in JSON: '" + name + "'");
            }
        }
        this.allowedTiles = parsedTypes;
    }

    // === Standard Getters ===

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public List<String> getAllowedPlants() {
        return allowedPlants;
    }

    public List<Level> getLevels() {
        return Collections.unmodifiableList(levels);
    }

    public List<TileType> getAllowedTiles() {
        return Collections.unmodifiableList(allowedTiles);
    }
}