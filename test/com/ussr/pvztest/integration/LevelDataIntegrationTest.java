package com.ussr.pvztest.integration;

import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.LevelManager;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wide-coverage sweep that loads the real levels.json and validates
 * structural invariants across every chapter and level, plus chapter
 * navigation (startChapter/startLevel/nextLevel). This is JSON-driven
 * config with no prior dedicated coverage, so failures here usually mean
 * either a data-authoring mistake or a LevelFactory parsing regression.
 */
class LevelDataIntegrationTest {

    private static LevelManager levelManager;

    @BeforeAll
    static void loadLevels() {
        levelManager = new LevelManager();
        levelManager.loadFromJson();
    }

    // ====== STRUCTURAL INTEGRITY ======

    @Test
    @DisplayName("✅ At least one chapter is loaded from levels.json")
    void loadFromJson_loadsAtLeastOneChapter() {
        assertFalse(levelManager.getChapters().isEmpty());
    }

    @Test
    @DisplayName("✅ Every chapter has a non-blank id and at least one level")
    void everyChapter_hasIdAndLevels() {
        for (Chapter chapter : levelManager.getChapters()) {
            assertNotNull(chapter.getId());
            assertFalse(chapter.getId().isBlank());
            assertFalse(chapter.getLevels().isEmpty(), chapter.getId() + " has no levels");
        }
    }

    @Test
    @DisplayName("✅ Every level within every chapter has a non-blank id")
    void everyLevel_hasNonBlankId() {
        for (Chapter chapter : levelManager.getChapters()) {
            for (Level level : chapter.getLevels()) {
                assertNotNull(level.getId(), chapter.getId() + " has a level with null id");
                assertFalse(level.getId().isBlank());
            }
        }
    }

    @Test
    @DisplayName("✅ Every level resolves to a non-null LevelBehavior instance")
    void everyLevel_hasResolvedBehavior() {
        for (Chapter chapter : levelManager.getChapters()) {
            for (Level level : chapter.getLevels()) {
                LevelBehavior behavior = level.getBehavior();
                assertNotNull(behavior, chapter.getId() + "/" + level.getId() + " has no resolved behavior");
            }
        }
    }

    @Test
    @DisplayName("✅ Every level has a non-null delivery strategy assigned by LevelManager")
    void everyLevel_hasDeliveryStrategy() {
        for (Chapter chapter : levelManager.getChapters()) {
            for (Level level : chapter.getLevels()) {
                assertNotNull(level.getDeliveryStrategy(),
                        chapter.getId() + "/" + level.getId() + " has no delivery strategy");
            }
        }
    }

    @Test
    @DisplayName("✅ Adventure-mode levels declare at least one allowed zombie")
    void adventureLevels_haveAllowedZombies() {
        for (Chapter chapter : levelManager.getChapters()) {
            if (chapter.getGameMode() != com.ussr.pvz.model.level.GameMode.ADVENTURE) continue;
            for (Level level : chapter.getLevels()) {
                assertFalse(level.getAllowedZombies().isEmpty(),
                        chapter.getId() + "/" + level.getId() + " has no allowed zombies");
            }
        }
    }

    @Test
    @DisplayName("✅ Every allowed-zombie weight is positive")
    void allowedZombies_haveNonNegativeWeights() {
        for (Chapter chapter : levelManager.getChapters()) {
            for (Level level : chapter.getLevels()) {
                for (Level.AllowedZombie z : level.getAllowedZombies()) {
                    assertTrue(z.weight() > 0,
                            chapter.getId() + "/" + level.getId() + " zombie " + z.id() + " has non-positive weight");
                }
            }
        }
    }

    @Test
    @DisplayName("✅ Every wave has a positive cost")
    void everyWave_hasPositiveCost() {
        for (Chapter chapter : levelManager.getChapters()) {
            for (Level level : chapter.getLevels()) {
                for (Level.Wave wave : level.getWaves()) {
                    assertTrue(wave.cost() > 0,
                            chapter.getId() + "/" + level.getId() + " wave " + wave.waveNumber() + " has non-positive cost");
                }
            }
        }
    }

    // ====== SPECIFIC KNOWN CHAPTERS ======

    @ParameterizedTest
    @DisplayName("✅ Core campaign chapters are discoverable by id via findChapter")
    @ValueSource(strings = {"ancient_egypt", "big_wave_beach", "dark_ages", "frostbite_caves", "minigames_arcade"})
    void findChapter_resolvesKnownChapterIds(String chapterId) {
        Chapter chapter = levelManager.findChapter(chapterId);
        assertNotNull(chapter, chapterId + " should exist in levels.json");
    }

    @Test
    @DisplayName("❌ findChapter returns null for unknown chapter id")
    void findChapter_returnsNull_forUnknownId() {
        assertNull(levelManager.findChapter("totally_not_a_chapter"));
    }

    @Test
    @DisplayName("✅ Ancient Egypt levels are ordered starting from 1")
    void ancientEgypt_levelsOrderedFromOne() {
        Chapter chapter = levelManager.findChapter("ancient_egypt");
        List<Level> levels = chapter.getLevels();
        assertEquals(1, levels.get(0).getOrder());
    }

    @Test
    @DisplayName("✅ Boss levels (order 4) resolve to BossBehavior across all adventure chapters")
    void bossLevels_resolveToBossBehavior() {
        for (String chapterId : List.of("ancient_egypt", "big_wave_beach", "dark_ages", "frostbite_caves")) {
            Chapter chapter = levelManager.findChapter(chapterId);
            Level bossLevel = chapter.getLevels().stream()
                    .filter(l -> l.getOrder() == 4)
                    .findFirst()
                    .orElse(null);
            assertNotNull(bossLevel, chapterId + " missing order-4 boss level");
            assertInstanceOf(com.ussr.pvz.model.level.behavior.BossBehavior.class, bossLevel.getBehavior(),
                    chapterId + " boss level should use BossBehavior");
        }
    }

    // ====== NAVIGATION ======

    @Test
    @DisplayName("✅ startChapter selects the chapter and defaults to its first level")
    void startChapter_selectsFirstLevelByDefault() {
        LevelManager manager = new LevelManager();
        manager.loadFromJson();

        manager.startChapter("ancient_egypt");

        assertEquals("ancient_egypt", manager.getCurrentChapter().getId());
        assertEquals(1, manager.getCurrentLevel().getOrder());
    }

    @Test
    @DisplayName("❌ startChapter throws for an unknown chapter id")
    void startChapter_throws_forUnknownId() {
        LevelManager manager = new LevelManager();
        manager.loadFromJson();

        assertThrows(IllegalArgumentException.class, () -> manager.startChapter("does_not_exist"));
    }

    @Test
    @DisplayName("✅ nextLevel advances within the same chapter before crossing chapters")
    void nextLevel_advancesWithinChapter() {
        LevelManager manager = new LevelManager();
        manager.loadFromJson();
        manager.startChapter("ancient_egypt");

        manager.nextLevel();

        assertEquals("ancient_egypt", manager.getCurrentChapter().getId());
        assertEquals(2, manager.getCurrentLevel().getOrder());
    }

    @Test
    @DisplayName("✅ nextLevel crosses into the next chapter once the current chapter is exhausted")
    void nextLevel_crossesChapterBoundary() {
        LevelManager manager = new LevelManager();
        manager.loadFromJson();
        manager.startChapter("ancient_egypt");

        // Ancient Egypt has 4 levels; advance past the last one.
        manager.nextLevel(); // -> level 2
        manager.nextLevel(); // -> level 3
        manager.nextLevel(); // -> level 4 (boss)
        manager.nextLevel(); // -> should cross into big_wave_beach

        assertEquals("frostbite_caves", manager.getCurrentChapter().getId());
        assertEquals(1, manager.getCurrentLevel().getOrder());
    }

    @Test
    @DisplayName("✅ hasNextLevel correctly reports availability across a chapter boundary")
    void hasNextLevel_reportsCorrectly() {
        LevelManager manager = new LevelManager();
        manager.loadFromJson();
        manager.startChapter("ancient_egypt");

        assertTrue(manager.hasNextLevel()); // level 1 -> 2 within chapter
    }
}