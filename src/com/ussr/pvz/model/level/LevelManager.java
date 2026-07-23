package com.ussr.pvz.model.level;

import com.ussr.pvz.model.engine.NewsObserver;
import com.ussr.pvz.model.level.delivery.ConveyorDeliveryStrategy;
import com.ussr.pvz.model.level.delivery.DeliveryStrategy;
import com.ussr.pvz.model.level.delivery.RegularDeliveryStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelManager {
    private final List<Chapter> chapters = new ArrayList<>();
    private final Map<String, JsonContainer.JsonLevelData> levelConfigs = new HashMap<>();
    private Chapter currentChapter;
    private Level currentLevel;

    private final LevelLoader loader = new LevelLoader();

    public void loadFromJson() {
        loadFromJson(null);
    }

    public void loadFromJson(String path) {
        JsonContainer.JsonWorldData world = path != null
                ? loader.load(path)
                : loader.load();

        if (world == null || world.chapters == null) {
            System.err.println("[LevelManager] Critical: Loaded world data or chapters array is null.");
            return;
        }

        chapters.clear();
        levelConfigs.clear();

        for (JsonContainer.JsonChapterData chapterData : world.chapters) {
            GameMode mode = parseGameMode(chapterData.gameMode);
            Chapter chapter = new Chapter(chapterData.id, chapterData.name, mode, new ArrayList<>());

            if (chapterData.allowedPlants != null) {
                chapter.setAllowedPlants(chapterData.allowedPlants);
            }

            if (chapterData.allowedTiles != null) {
                chapter.setAllowedTiles(chapterData.allowedTiles);
            }

            if (chapterData.levels != null) {
                for (JsonContainer.JsonLevelData levelData : chapterData.levels) {
                    levelConfigs.put(levelData.id, levelData);
                    Level level = LevelFactory.create(levelData);
                    level.setChapter(chapter.getId());
                    level.setDeliveryStrategy(buildDeliveryStrategy(levelData.deliveryStrategy,level));
                    chapter.addLevel(level);
                }
            }

            chapters.add(chapter);
        }
        System.out.println("[LevelManager] Successfully loaded " + chapters.size() + " chapters into memory.");
    }

    public void startChapter(String id) {
        if (id == null)
            throw new IllegalArgumentException("Chapter ID cannot be null");

        currentChapter = chapters.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Chapter not found: " + id));

        if (currentChapter.getLevels().isEmpty())
            throw new IllegalStateException(
                    "Chapter '" + id + "' contains no levels.");

        // automatically prepare the first level
        startLevel(currentChapter.getLevels().getFirst().getId());
    }

    public void startLevel(String id) {
        if (currentChapter == null)
            throw new IllegalStateException("No chapter selected.");

        if (currentChapter.findLevel(id).isEmpty())
            throw new IllegalArgumentException("Level not found: " + id);

        refreshLevelState(id);
    }

    public void nextLevel() {
        if (currentChapter == null || currentLevel == null)
            throw new IllegalStateException("No active level.");
        List<Level> levels = currentChapter.getLevels();

        int currentIndex = -1;

        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).getId().equals(currentLevel.getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1)
            throw new IllegalStateException(
                    "Current level is not part of the current chapter.");

        // ---------- Next level in same chapter ----------

        if (currentIndex + 1 < levels.size()) {
            startLevel(levels.get(currentIndex + 1).getId());
            return;
        }

        // ---------- Next chapter ----------

        int chapterIndex = chapters.indexOf(currentChapter);

        if (chapterIndex + 1 < chapters.size()) {

            currentChapter = chapters.get(chapterIndex + 1);

            if (currentChapter.getLevels().isEmpty())
                throw new IllegalStateException(
                        "Next chapter has no levels.");

            startLevel(currentChapter.getLevels().getFirst().getId());

            System.out.println(
                    "[LevelManager] Chapter completed! Advancing to: "
                            + currentChapter.getName());

            return;
        }

        System.out.println(
                "[LevelManager] Final campaign completed.");
    }

    private void refreshLevelState(String levelId) {
        JsonContainer.JsonLevelData data = levelConfigs.get(levelId);

        if (data == null)
            throw new IllegalArgumentException(
                    "No JSON configuration found for level: " + levelId);

        Level fresh = LevelFactory.create(data);

        fresh.setChapter(currentChapter.getId());
        fresh.setDeliveryStrategy(buildDeliveryStrategy(data.deliveryStrategy,fresh));

        currentLevel = fresh;
    }

    public boolean hasNextLevel() {

        if (currentChapter == null || currentLevel == null)
            return false;

        List<Level> levels = currentChapter.getLevels();

        int currentIndex = -1;

        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).getId().equals(currentLevel.getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1)
            return false;

        if (currentIndex + 1 < levels.size())
            return true;

        int chapterIndex = chapters.indexOf(currentChapter);

        return chapterIndex + 1 < chapters.size();
    }

    public void completeCurrentLevel() {
        if (currentLevel != null) {
            currentLevel.onComplete();
        }
    }

    public Chapter getCurrentChapter() {
        return currentChapter;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public List<Chapter> getChapters() {
        return Collections.unmodifiableList(chapters);
    }

    public Chapter findChapter(String id) {
        if (id == null) return null;
        return chapters.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private GameMode parseGameMode(String raw) {
        if (raw == null || raw.isBlank()) return GameMode.ADVENTURE;

        return switch (raw.trim().toUpperCase()) {
            case "MINIGAME" -> GameMode.MINIGAME;
            case "MEOW" -> GameMode.MEOW;
            default -> GameMode.ADVENTURE;
        };
    }

    private DeliveryStrategy buildDeliveryStrategy(String raw,Level level) {
        if (raw == null || raw.isBlank()) return new RegularDeliveryStrategy();

        return switch (raw.trim().toLowerCase()) {
            case "conveyor" -> {
                ConveyorDeliveryStrategy strategy = new ConveyorDeliveryStrategy();
                strategy.setAvailablePlants(level.getSeedPlants());
                yield strategy;
            }
            default -> new RegularDeliveryStrategy();
        };
    }
}