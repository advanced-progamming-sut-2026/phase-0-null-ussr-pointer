package com.ussr.pvz.model.quest;

import java.util.List;

public class ConfigurableQuest implements Quest {

    private final String id;
    private final String title;
    private final QuestType type;
    private final QuestPriority priority;
    private final List<CriterionProgress> criteria;
    private final QuestReward reward;
    private final Long expiresAfterSeconds;
    private final long createdAt;
    private boolean completed;

    public ConfigurableQuest(String id, String title, QuestType type, QuestPriority priority,
                             List<CriterionProgress> criteria, QuestReward reward,
                             Long expiresAfterSeconds) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.priority = priority;
        this.criteria = criteria;
        this.reward = reward;
        this.expiresAfterSeconds = expiresAfterSeconds;
        this.createdAt = System.currentTimeMillis();
        this.completed = false;
    }

    @Override
    public void onProgress(String eventType, int amount, QuestContext ctx) {
        if (completed || isExpired()) {
            return;
        }

        for (CriterionProgress criterion : criteria) {
            if (criterion.getType().equalsIgnoreCase(eventType) && contextMatches(criterion, ctx)) {
                criterion.increment(amount);
            }
        }
        checkCompletion();
    }

    @Override
    public void onLevelEnd(QuestContext ctx) {
        if (completed || isExpired()) {
            return;
        }
        onProgress("LEVEL_END", 1, ctx);
    }

    private void checkCompletion() {
        boolean allMet = criteria.stream().allMatch(CriterionProgress::isMet);
        if (allMet && !completed) {
            completed = true;
            // Apply reward and send notification
            QuestRewardApplier.applyReward(reward, title);
        }
    }

    private boolean contextMatches(CriterionProgress c, QuestContext ctx) {
        // Condition matching logic mapping to QuestContext state fields
        switch (c.getType().toUpperCase()) {
            case "WIN_LEVEL_EXACT_SUN_LEFT":
                return ctx.sunLeft == c.getInt("exactSunLeft", 0);
            case "WIN_LEVEL_MAX_SUN_PRODUCERS":
                return ctx.sunProducerCount <= c.getInt("maxSunProducers", Integer.MAX_VALUE);
            case "WIN_LEVEL_MAX_PLANTS_LOST":
                return ctx.plantsLost <= c.getInt("maxPlantsLost", Integer.MAX_VALUE);
            case "WIN_LEVEL_SYMMETRIC":
                return ctx.gardenSymmetric;
            case "WIN_LEVEL_ASYMMETRIC":
                return ctx.gardenAsymmetric;
            case "KILL_ZOMBIES_FIRST_COLUMN_NO_LAWNMOWER":
                return !ctx.hadLawnmower && ctx.columnIndex == 0;
            case "WIN_LEVEL_EMPTY_COLUMN":
                return ctx.emptyColumns != null && !ctx.emptyColumns.isEmpty();
            case "WIN_LEVEL_EMPTY_ROW":
                return ctx.emptyRows != null && !ctx.emptyRows.isEmpty();
            case "WIN_LEVEL_EMPTY_ROW_AND_COLUMN":
                return (ctx.emptyColumns != null && !ctx.emptyColumns.isEmpty()) &&
                       (ctx.emptyRows != null && !ctx.emptyRows.isEmpty());
            case "KILL_ZOMBIES_IN_CHAPTER":
                String chapter = c.getString("chapter");
                return chapter == null || chapter.equals("any") || chapter.equals(ctx.chapterId);
            case "KILL_ZOMBIES_WITH_SPECIFIC_PLANT":
                String plant = c.getString("plantType");
                if (plant == null || plant.equals("any_offensive")) return true;
                return ctx.plantKey != null && ctx.plantKey.toLowerCase().contains(plant.toLowerCase());
            case "KILL_ZOMBIES_TIME_LIMIT":
                int timeLimit = c.getInt("timeLimitSeconds", Integer.MAX_VALUE);
                return ctx.elapsedSeconds <= timeLimit;
            case "KILL_ZOMBIES_EXCLUSIVE_FAMILY":
                return true; // Additional logic can be implemented
            case "WIN_DAY_LEVEL_WITH_NIGHT_PLANTS":
                return true; // Additional logic for mushrooms can be implemented
            case "WIN_CONSECUTIVE_LEVELS_MAX_DIFFICULTY":
                return ctx.consecutiveWins >= c.getInt("difficulty", 1);
            default:
                return true;
        }
    }

    @Override
    public void reset() {
        this.completed = false;
        if (criteria != null) {
            criteria.forEach(CriterionProgress::reset);
        }
    }
    public void restoreProgress(boolean isCompleted, List<Integer> criteriaProgress) {
        this.completed = isCompleted;
        if (criteriaProgress != null && criteriaProgress.size() == this.criteria.size()) {
            for (int i = 0; i < criteriaProgress.size(); i++) {
                this.criteria.get(i).set(criteriaProgress.get(i));
            }
        }
    }
    @Override
    public boolean isExpired() {
        if (expiresAfterSeconds == null) {
            return false;
        }
        return (System.currentTimeMillis() - createdAt) > (expiresAfterSeconds * 1000);
    }

    @Override public String getId() { return id; }
    @Override public String getTitle() { return title; }
    @Override public QuestType getType() { return type; }
    @Override public QuestPriority getPriority() { return priority; }
    @Override public List<CriterionProgress> getCriteria() { return criteria; }
    @Override public QuestReward getReward() { return reward; }
    @Override public boolean isCompleted() { return completed; }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}