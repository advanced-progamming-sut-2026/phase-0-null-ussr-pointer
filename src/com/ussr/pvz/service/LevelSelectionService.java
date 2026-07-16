package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.model.level.Level;

import java.util.List;

public class LevelSelectionService {
    private String pendingCheatLevelId = null;

    public String showLevels() {
        Chapter chapter = App.getLevelManager().getCurrentChapter();
        if (chapter == null) return "No chapter selected.";

        int currentLvlProgress = App.getAccount().getAdventureProgress().getCurrentLvl();
        StringBuilder sb = new StringBuilder("--- Levels in " + chapter.getId() + " ---\n");

        List<Level> levels = chapter.getLevels();
        for (Level level : levels) {
            boolean unlocked = level.getOrder() <= currentLvlProgress;
            sb.append("- ").append(level.getId())
                    .append(unlocked ? " [UNLOCKED]" : " [LOCKED]")
                    .append("\n");
        }
        return sb.toString().trim();
    }

    public String selectLevel(String levelId) {
        Chapter chapter = App.getLevelManager().getCurrentChapter();
        if (chapter == null) return "No chapter selected.";

        Level targetLevel = null;
        for (Level l : chapter.getLevels()) {
            if (l.getId().equals(levelId)) {
                targetLevel = l;
                break;
            }
        }

        if (targetLevel == null) return "Level not found in this chapter.";

        int currentLvlProgress = App.getAccount().getAdventureProgress().getCurrentLvl();

        // Check if level is locked based on user order progress
        if (targetLevel.getOrder() > currentLvlProgress) {
            pendingCheatLevelId = levelId;
            return "This level is locked! Do you want to use a cheat code to enter? (yes/no)";
        }

        return enterLevel(targetLevel, false);
    }

    public String confirmCheat(String answer) {
        if (pendingCheatLevelId == null) return "invalid command (no level pending cheat confirmation).";

        if (answer.equalsIgnoreCase("yes")) {
            Chapter chapter = App.getLevelManager().getCurrentChapter();
            Level targetLevel = null;
            for (Level l : chapter.getLevels()) {
                if (l.getId().equals(pendingCheatLevelId)) {
                    targetLevel = l;
                    break;
                }
            }
            pendingCheatLevelId = null;
            if (targetLevel != null) {
                return enterLevel(targetLevel, true);
            }
            return "Error finding level.";
        } else {
            pendingCheatLevelId = null;
            return "Cheat canceled. Please select another level.";
        }
    }

    private String enterLevel(Level level, boolean isCheated) {
        App.setCheatedLevel(isCheated);
        // Load the level configuration to the manager
        //App.getGameSession().setLevel(level);
        // Transition to plant selection prep phase
        App.setMenuState(MenuState.CHOOSE_PLANT);

        return isCheated
                ? "Cheat enabled! Entering locked level: " + level.getId() + " (Progress will NOT be saved)"
                : "Entering level: " + level.getId();
    }
}