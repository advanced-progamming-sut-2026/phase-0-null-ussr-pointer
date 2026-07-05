package com.ussr.pvz.model.quest;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.event.GameEventBus;

public class QuestEventTracker {

    private final QuestManager questManager;

    private int sessionPlantsLost = 0;
    private boolean lawnmowerTriggered = false;

    public QuestEventTracker(QuestManager questManager) {
        this.questManager = questManager;
    }

    public void subscribeTo(GameSession session) {
        GameEventBus eventBus = session.getEventBus();

        eventBus.subscribe(GameEvent.SunCollected.class, event -> {
            QuestContext ctx = new QuestContext();
            questManager.onGameEvent("COLLECT_SUN", event.value(), ctx);
        });

        eventBus.subscribe(GameEvent.PlantDied.class, event -> sessionPlantsLost++);
        eventBus.subscribe(GameEvent.LawnMowerTriggered.class, event -> lawnmowerTriggered = true);

        eventBus.subscribe(GameEvent.PlantPlanted.class, event -> {
            String name = event.plantName().toLowerCase();
            if (name.contains("cherry") || name.contains("bomb") || name.contains("mine") || name.contains("jalapeno")) {
                QuestContext ctx = new QuestContext();
                questManager.onGameEvent("USE_EXPLOSIVE_PLANTS", 1, ctx);
            }
        });

        eventBus.subscribe(GameEvent.ZombieDied.class, event -> {
            QuestContext ctx = new QuestContext();
            ctx.rowIndex = (int) event.y();
            ctx.columnIndex = (int) event.x();
            ctx.plantKey = event.killerPlantName();
            ctx.hadLawnmower = lawnmowerTriggered;
            ctx.elapsedSeconds = (int) session.getElapsedSeconds();

            if (App.getAccount() != null) {
                ctx.chapterId = String.valueOf(App.getAccount().getAdventureProgress().getCurrentChapter());
            } else {
                ctx.chapterId = "any";
            }

            questManager.onGameEvent("KILL_ZOMBIES_WITH_SPECIFIC_PLANT", 1, ctx);
            questManager.onGameEvent("KILL_ZOMBIES_IN_CHAPTER", 1, ctx);
            questManager.onGameEvent("KILL_ZOMBIES_TIME_LIMIT", 1, ctx);
            questManager.onGameEvent("KILL_ZOMBIES_EXCLUSIVE_FAMILY", 1, ctx);

            if (!ctx.hadLawnmower && ctx.columnIndex == 0) {
                questManager.onGameEvent("KILL_ZOMBIES_FIRST_COLUMN_NO_LAWNMOWER", 1, ctx);
            }
        });

        eventBus.subscribe(GameEvent.GameWon.class, event -> {
            QuestContext ctx = new QuestContext();
            ctx.sunLeft = session.getSunCount();
            ctx.plantsLost = sessionPlantsLost;
            ctx.sunProducerCount = countSunProducers(session);
            ctx.gardenSymmetric = checkSymmetry(session);
            ctx.gardenAsymmetric = !ctx.gardenSymmetric;

            questManager.onLevelEnd(ctx);
            questManager.onGameEvent("WIN_LEVEL_EXACT_SUN_LEFT", 1, ctx);
            questManager.onGameEvent("WIN_LEVEL_MAX_PLANTS_LOST", 1, ctx);
            questManager.onGameEvent("WIN_LEVEL_SYMMETRIC", 1, ctx);
            questManager.onGameEvent("WIN_LEVEL_ASYMMETRIC", 1, ctx);
            questManager.onGameEvent("WIN_LEVEL_MAX_SUN_PRODUCERS", 1, ctx);
            questManager.onGameEvent("WIN_DAY_LEVEL_WITH_NIGHT_PLANTS", 1, ctx);
        });
    }

    private int countSunProducers(GameSession session) {
        if (session.getPlants() == null) return 0;
        return (int) session.getPlants().stream()
                .filter(p -> p.getName().toLowerCase().contains("sunflower") || p.getName().toLowerCase().contains("shroom"))
                .count();
    }

    private boolean checkSymmetry(GameSession session) {
        if (session.getLawn() == null) return false;
        int rows = session.getLawn().getRows();
        int cols = session.getLawn().getCols();

        for (int r = 0; r < rows / 2; r++) {
            int mirrorR = rows - 1 - r;
            for (int c = 0; c < cols; c++) {
                boolean hasPlant1 = session.getLawn().getCell(r, c).getPlant() != null;
                boolean hasPlant2 = session.getLawn().getCell(mirrorR, c).getPlant() != null;

                if (hasPlant1 != hasPlant2) {
                    return false;
                }
            }
        }
        return true;
    }
}