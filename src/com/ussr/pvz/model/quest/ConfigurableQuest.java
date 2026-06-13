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
    }

    @Override
    public void onProgress(String eventType, int amount, QuestContext ctx) {
    }

    @Override
    public void onLevelEnd(QuestContext ctx) {
    }

    @Override
    public void onComplete() {  }

    @Override
    public void reset() {
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    private boolean contextMatches(CriterionProgress c, QuestContext ctx) {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public QuestType getType() {
        return type;
    }

    @Override
    public QuestPriority getPriority() {
        return priority;
    }

    @Override
    public List<CriterionProgress> getCriteria() {
        return criteria;
    }

    @Override
    public QuestReward getReward() {
        return reward;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }
}

