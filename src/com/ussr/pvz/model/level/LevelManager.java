package com.ussr.pvz.model.level;

import com.ussr.pvz.model.level.delivery.ConveyorDeliveryStrategy;
import com.ussr.pvz.model.level.delivery.DeliveryStrategy;
import com.ussr.pvz.model.level.delivery.RegularDeliveryStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LevelManager {
    private List<Chapter> chapters = new ArrayList<>();
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

        if (world == null || world.chapters == null) return;

        chapters.clear();

        for (JsonContainer.JsonChapterData chapterData : world.chapters) {
            GameMode mode = parseGameMode(chapterData.gameMode);
            Chapter chapter = new Chapter(chapterData.id, chapterData.name, mode, List.of());

            if (chapterData.allowedPlants != null)
                chapter.setAllowedPlants(chapterData.allowedPlants);

            if (chapterData.levels != null) {
                for (JsonContainer.JsonLevelData levelData : chapterData.levels) {
                    Level level = LevelFactory.create(levelData);
                    level.setDeliveryStrategy(buildDeliveryStrategy(levelData.deliveryStrategy));
                    level.setChapterId(chapterData.id);
                    chapter.addLevel(level);
                }
            }

            chapters.add(chapter);
        }
    }

    public void startChapter(String id) {
        currentChapter = chapters.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("chapter not found: " + id));
        currentLevel = currentChapter.getLevels().getFirst();
    }

    public void startLevel(String id) {
        currentLevel = currentChapter.findLevel(id)
                .orElseThrow(() -> new IllegalArgumentException("level not found: " + id));
        currentLevel.onStart();
    }

    public void nextLevel() {
        List<Level> levels = currentChapter.getLevels();
        int currentIndex = -1;
        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).getId().equals(currentLevel.getId())) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1)
            throw new IllegalStateException("current level not found in chapter");
        if (currentIndex + 1 >= levels.size())
            throw new IllegalStateException("no more levels in chapter: " + currentChapter.getId());

        currentLevel = levels.get(currentIndex + 1);
        currentLevel.onStart();
    }

    public boolean hasNextLevel() {
        List<Level> levels = currentChapter.getLevels();
        int currentIndex = levels.indexOf(currentLevel);
        return currentIndex >= 0 && currentIndex + 1 < levels.size();
    }

    public void completeCurrentLevel() {
        currentLevel.onComplete();
    }

    public Chapter getCurrentChapter() { return currentChapter; }
    public Level getCurrentLevel() { return currentLevel; }
    public List<Chapter> getChapters() { return Collections.unmodifiableList(chapters); }

    public Chapter findChapter(String id) {
        return chapters.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private GameMode parseGameMode(String raw) {
        if (raw == null) return GameMode.ADVENTURE;
        return switch (raw.toUpperCase()) {
            case "MINIGAME" -> GameMode.MINIGAME;
            case "BOUNCE" -> GameMode.BOUNCE;
            default -> GameMode.ADVENTURE;
        };
    }

    private DeliveryStrategy buildDeliveryStrategy(String raw) {
        if (raw == null) return new RegularDeliveryStrategy();
        return switch (raw.toLowerCase()) {
            case "conveyor" -> new ConveyorDeliveryStrategy();
            default -> new RegularDeliveryStrategy();
        };
    }
}