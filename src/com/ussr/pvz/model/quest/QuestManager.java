package com.ussr.pvz.model.quest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class QuestManager {

    private final List<ConfigurableQuest> allQuests = new ArrayList<>();
    private final Gson gson = new Gson();

    public void loadFromJson() throws FileNotFoundException {
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("src/resources/quests.json")))) {

            Type questListType = new TypeToken<ArrayList<ConfigurableQuest>>() {}.getType();
            List<ConfigurableQuest> loadedQuests = gson.fromJson(reader, questListType);

            if (loadedQuests != null) {
                allQuests.clear();
                allQuests.addAll(loadedQuests);
            }
        } catch (Exception e) {
            throw new FileNotFoundException(e.getMessage());
        }
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