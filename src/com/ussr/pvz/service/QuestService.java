package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.level.GameMode;
import com.ussr.pvz.model.quest.ConfigurableQuest;
import com.ussr.pvz.model.quest.QuestManager;
import com.ussr.pvz.model.quest.QuestType;

import java.util.List;

public class QuestService {

    public String getPage(String pageName) {
        if (App.getAccount() == null) {
            return "Error: No active account logged in.";
        }

        QuestManager qm = App.getAccount().getQuestManager();
        QuestType requestedType;

        try {
            requestedType = QuestType.fromString(pageName.toLowerCase());
        } catch (IllegalArgumentException e) {
            return "Error: Invalid travel log page. Available pages: daily, challenge, epic.";
        }

        List<ConfigurableQuest> activeQuests = qm.getByType(requestedType);

        if (activeQuests.isEmpty()) {
            return "No active " + pageName + " quests available.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("--- ").append(pageName.toUpperCase()).append(" QUESTS ---\n");
        for (ConfigurableQuest q : activeQuests) {
            sb.append(String.format("- [%s] %s (Priority: %s)\n",
                    q.isCompleted() ? "X" : " ",
                    q.getTitle(),
                    q.getPriority()));

            q.getCriteria().forEach(c ->
                    sb.append(String.format("   Progress: %d / %d\n", c.getCurrent(), c.getTarget()))
            );
        }
        return sb.toString();
    }

    public String playMinigame(String levelId) {
        try {
            // Load the arcade chapter dynamically
            App.getLevelManager().startChapter("minigames_arcade");
            com.ussr.pvz.model.level.Chapter chapter = App.getLevelManager().getCurrentChapter();

            if (chapter != null) {
                com.ussr.pvz.model.level.Level target = null;
                for (com.ussr.pvz.model.level.Level l : chapter.getLevels()) {
                    if (l.getId().equals(levelId)) {
                        target = l;
                        break;
                    }
                }
                if (target != null) {
                    App.setCheatedLevel(false); // Minigames don't strictly use adventure progress
                    App.getLevelManager().startLevel(target.getId());
                    App.setMenuState(MenuState.CHOOSE_PLANT); // Jump to game prep
                    return "Starting minigame: " + levelId;
                }
            }
            return "Minigame not found: " + levelId;
        } catch (Exception e) {
            return "Error starting minigame: " + e.getMessage();
        }
    }

    public String showMinigames() {
        StringBuilder sb = new StringBuilder();

        App.getLevelManager().getChapters().stream()
                .filter(chapter -> chapter.getGameMode().equals(GameMode.MINIGAME))
                .forEach(chapter -> {
                    sb.append("=== Minigame Chapter ===\n");
                    chapter.getLevels().forEach(level ->
                            sb.append(" - Level ID: ").append(level.getId()).append("\n")
                    );
                    sb.append("------------------------\n");
                });

        return sb.toString();
    }
}