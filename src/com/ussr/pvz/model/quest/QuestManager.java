package com.ussr.pvz.model.quest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuestManager {

    private final List<ConfigurableQuest> allQuests = new ArrayList<>();
    private final Gson gson = new Gson();

    public void loadFromJson() throws FileNotFoundException {
        java.io.File file = new java.io.File("src/resources/quests.json");
        if (!file.exists()) {
            throw new FileNotFoundException("quests.json not found!");
        }

        try (java.io.FileReader reader = new java.io.FileReader(file)) {
            Type questListType = new TypeToken<ArrayList<ConfigurableQuest>>() {}.getType();
            List<ConfigurableQuest> loadedQuests = gson.fromJson(reader, questListType);

            if (loadedQuests != null) {
                allQuests.clear();
                long now = System.currentTimeMillis();
                for (ConfigurableQuest q : loadedQuests) {
                    q.initCreatedAt(now);
                }
                allQuests.addAll(loadedQuests);
            }
        } catch (Exception e) {
            throw new FileNotFoundException("Error parsing quests: " + e.getMessage());
        }
    }

    public void restoreState(List<String> completedIds, Map<String, List<Integer>> progressMap) {
        for (ConfigurableQuest q : allQuests) {
            boolean isCompleted = completedIds != null && completedIds.contains(q.getId());
            List<Integer> progress = progressMap != null ? progressMap.get(q.getId()) : null;
            q.restoreProgress(isCompleted, progress);
        }
    }

    // CHANGED: Added export logic for AccountState serialization
    public Map<String, List<Integer>> exportProgressMap() {
        return allQuests.stream()
                .filter(q -> !q.isCompleted() && !q.isExpired())
                .collect(Collectors.toMap(
                        ConfigurableQuest::getId,
                        q -> q.getCriteria().stream().map(CriterionProgress::getCurrent).collect(Collectors.toList())
                ));
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
        return allQuests.stream()
                .filter(q -> !q.isCompleted() && !q.isExpired())
                .collect(Collectors.toList());
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