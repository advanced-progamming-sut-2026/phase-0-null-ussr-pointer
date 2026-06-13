package com.ussr.pvz.model.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestManager {

    private final List<ConfigurableQuest> allQuests = new ArrayList<>();

    public void loadFromJson() {
    }

    public void onGameEvent(String eventType, int amount, QuestContext ctx) {
        getActive().forEach(q -> q.onProgress(eventType, amount, ctx));
    }

    public void onLevelEnd(QuestContext ctx) {
        getActive().forEach(q -> q.onLevelEnd(ctx));
    }

    public void resetDaily() {
        allQuests.stream()
                .filter(q -> q.getType() == QuestType.DAILY)
                .forEach(ConfigurableQuest::reset);
    }

    public List<ConfigurableQuest> getActive() {
        return allQuests.stream().filter(q -> !q.isCompleted() && !q.isExpired()).collect(Collectors.toList());
    }

    public List<ConfigurableQuest> getCompleted() {
        return allQuests.stream().filter(ConfigurableQuest::isCompleted).collect(Collectors.toList());
    }

    public List<ConfigurableQuest> getByType(QuestType type) {
        return allQuests.stream().filter(q -> q.getType() == type).collect(Collectors.toList());
    }

    public List<ConfigurableQuest> getByPriority(QuestPriority p) {
        return allQuests.stream().filter(q -> q.getPriority() == p).collect(Collectors.toList());
    }
}

