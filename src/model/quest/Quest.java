package model.quest;

import java.util.List;

public interface Quest {
    String getId();

    String getTitle();

    QuestType getType();

    QuestPriority getPriority();

    List<CriterionProgress> getCriteria();

    QuestReward getReward();

    boolean isCompleted();

    boolean isExpired();

    void onProgress(String eventType, int amount, QuestContext ctx);

    void onLevelEnd(QuestContext ctx);

    void onComplete();

    void reset();
}
