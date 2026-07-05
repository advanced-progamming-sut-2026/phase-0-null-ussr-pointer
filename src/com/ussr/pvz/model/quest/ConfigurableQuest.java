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
        onProgress("LEVEL_END", 1, ctx);
    }

    private void checkCompletion() {
        boolean allMet = criteria.stream().allMatch(CriterionProgress::isMet);
        if (allMet && !completed) {
            completed = true;
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
}