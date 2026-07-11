package com.ussr.pvz.model.level;

import com.ussr.pvz.model.level.delivery.ConveyorDeliveryStrategy;
import com.ussr.pvz.model.level.delivery.DeliveryStrategy;
import com.ussr.pvz.model.level.delivery.RegularDeliveryStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LevelManager {
    private final List<Chapter> chapters = new ArrayList<>();
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

        for (JsonContainer.JsonChapterData chapterData : world.chapters) {
            GameMode mode = parseGameMode(chapterData.gameMode);

            // Instantiating Chapter safely
            Chapter chapter = new Chapter(chapterData.id, chapterData.name, mode, new ArrayList<>());

            if (chapterData.allowedPlants != null) {
                chapter.setAllowedPlants(chapterData.allowedPlants);
            }

            // Bind world-specific tile layouts (Ancient Egypt sand, BWB water, Frostbite ice)
            if (chapterData.allowedTiles != null) {
                chapter.setAllowedTiles(chapterData.allowedTiles);
            }

            if (chapterData.levels != null) {
                for (JsonContainer.JsonLevelData levelData : chapterData.levels) {
                    Level level = LevelFactory.create(levelData);
                    level.setChapter(chapter.getId());
                    level.setDeliveryStrategy(buildDeliveryStrategy(levelData.deliveryStrategy));
                    level.setChapterId(chapterData.id);
                    chapter.addLevel(level);
                }
            }

            chapters.add(chapter);
        }
        System.out.println("[LevelManager] Successfully loaded " + chapters.size() + " chapters into memory.");
    }

    public void startChapter(String id) {
        if (id == null) throw new IllegalArgumentException("Chapter ID cannot be null");

        currentChapter = chapters.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found: " + id));

        if (currentChapter.getLevels().isEmpty()) {
            throw new IllegalStateException("Cannot start chapter '" + id + "' because it contains no levels.");
        }

        currentLevel = currentChapter.getLevels().getFirst();
    }

    public void startLevel(String id) {
        if (currentChapter == null) {
            throw new IllegalStateException("Cannot start a level before a chapter has been initialized.");
        }
        if (id == null) throw new IllegalArgumentException("Level ID cannot be null");

        currentLevel = currentChapter.findLevel(id)
                .orElseThrow(() -> new IllegalArgumentException("Level '" + id + "' not found in chapter: " + currentChapter.getId()));

        currentLevel.onStart();
    }

    /**
     * Advances the campaign framework state machine seamlessly.
     * Prevents hard crashes by transitioning across chapter borders automatically.
     */
    public void nextLevel() {
        if (currentChapter == null || currentLevel == null) {
            throw new IllegalStateException("Cannot advance level; active session state is missing.");
        }

        List<Level> levels = currentChapter.getLevels();
        int currentIndex = -1;

        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).getId().equals(currentLevel.getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            throw new IllegalStateException("System error: current level tracking broken out of its parent chapter collection.");
        }

        // 1. If there's another level left in the current world, advance normally
        if (currentIndex + 1 < levels.size()) {
            currentLevel = levels.get(currentIndex + 1);
            currentLevel.onStart();
            return;
        }

        // 2. Out of levels! Attempt to safely cascade into the next chapter
        int currentChapterIndex = chapters.indexOf(currentChapter);
        if (currentChapterIndex >= 0 && currentChapterIndex + 1 < chapters.size()) {
            currentChapter = chapters.get(currentChapterIndex + 1);

            if (currentChapter.getLevels().isEmpty()) {
                throw new IllegalStateException("Campaign transition failed: Next chapter '" + currentChapter.getId() + "' has zero levels configured.");
            }

            currentLevel = currentChapter.getLevels().getFirst();
            currentLevel.onStart();
            System.out.println("[LevelManager] Chapter completed! Advancing to: " + currentChapter.getName());
        } else {
            // 3. Campaign completed fully! End-of-game victory boundary
            System.out.println("[LevelManager] Final campaign chapter fully cleared! Triggering Game Cleared Engine UI Event.");
        }
    }

    public boolean hasNextLevel() {
        if (currentChapter == null || currentLevel == null) return false;

        List<Level> levels = currentChapter.getLevels();
        int currentIndex = levels.indexOf(currentLevel);

        // True if there is another level in this chapter
        if (currentIndex >= 0 && currentIndex + 1 < levels.size()) return true;

        // True if we are at the end of this chapter, but there is an entire next world loaded
        int currentChapterIndex = chapters.indexOf(currentChapter);
        return currentChapterIndex >= 0 && currentChapterIndex + 1 < chapters.size();
    }

    public void completeCurrentLevel() {
        if (currentLevel != null) {
            currentLevel.onComplete();
        }
    }

    // === Encapsulated Accessors ===

    public Chapter getCurrentChapter() { return currentChapter; }

    public Level getCurrentLevel() { return currentLevel; }

    public List<Chapter> getChapters() { return Collections.unmodifiableList(chapters); }

    public Chapter findChapter(String id) {
        if (id == null) return null;
        return chapters.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // === Internal Text Parsing Closures ===

    private GameMode parseGameMode(String raw) {
        if (raw == null || raw.isBlank()) return GameMode.ADVENTURE;

        return switch (raw.trim().toUpperCase()) {
            case "MINIGAME" -> GameMode.MINIGAME;
            case "BOUNCE"   -> GameMode.BOUNCE;
            default         -> GameMode.ADVENTURE;
        };
    }

    private DeliveryStrategy buildDeliveryStrategy(String raw) {
        if (raw == null || raw.isBlank()) return new RegularDeliveryStrategy();

        return switch (raw.trim().toLowerCase()) {
            case "conveyor" -> new ConveyorDeliveryStrategy();
            default         -> new RegularDeliveryStrategy();
        };
    }
}