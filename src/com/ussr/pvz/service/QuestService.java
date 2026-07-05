package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
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
}