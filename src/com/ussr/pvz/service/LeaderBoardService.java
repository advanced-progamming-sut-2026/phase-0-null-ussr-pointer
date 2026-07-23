package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.dto.LeaderBoardSortRequest;
import com.ussr.pvz.model.quest.ConfigurableQuest;
import com.ussr.pvz.model.quest.QuestType;

import java.util.Comparator;
import java.util.List;

public class LeaderBoardService {

    public LeaderBoardService() {
    }

    public String show() {
        List<Account> accounts = App.getAccounts();
        if (accounts.isEmpty()) {
            return "No accounts found.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-15s | %-15s | %-10s | %-12s | %-12s | %-10s\n",
                "Username", "Progress", "Minigames", "Daily Quests", "Other Quests", "MooPoints"));
        sb.append("-".repeat(85)).append("\n");

        for (Account acc : accounts) {
            String progress = "Ch:" + acc.getAdventureProgress().getCurrentChapter() +
                    " Lv:" + acc.getAdventureProgress().getCurrentLvl();

            int minigames = acc.getAdventureProgress().getMinigamesWon();
            int dailyQuests = getCompletedQuestCount(acc, QuestType.DAILY);
            int nonDailyQuests = getCompletedQuestCount(acc, QuestType.CHALLENGE)
                    + getCompletedQuestCount(acc, QuestType.EPIC);
            int score = acc.getScoreRecord().getScore();

            sb.append(String.format("%-15s | %-15s | %-10d | %-12d | %-12d | %-10d\n",
                    acc.getName(), progress, minigames, dailyQuests, nonDailyQuests, score));
        }

        return sb.toString();
    }

    public String sort(LeaderBoardSortRequest request) {
        String rawCol = request.column() != null ? request.column().toLowerCase().trim() : "score";
        String ord = request.order() != null ? request.order().toLowerCase().trim() : "desc";

        boolean isAsc = ord.equals("asc") || ord.equals("ascending") || ord.equals("+");

        Comparator<Account> comparator = getComparatorForColumn(rawCol);

        if (!isAsc) {
            comparator = comparator.reversed();
        }

        // Always tie-break with alphabetical username sorting
        comparator = comparator.thenComparing(Account::getName);
        App.getAccounts().sort(comparator);

        return "Leaderboard successfully sorted by '" + rawCol + "' in " +
                (isAsc ? "ascending" : "descending") + " order.\n\n" + show();
    }

    private int getCompletedQuestCount(Account acc, QuestType type) {
        if (acc.getQuestManager() == null) return 0;
        List<ConfigurableQuest> quests = acc.getQuestManager().getByType(type);
        if (quests == null) return 0;
        return (int) quests.stream().filter(ConfigurableQuest::isCompleted).count();
    }

    private Comparator<Account> getComparatorForColumn(String column) {
        return switch (column) {
            case "progress", "level", "chapter" ->
                    Comparator.comparingInt((Account a) -> a.getAdventureProgress().getCurrentChapter())
                            .thenComparingInt(a -> a.getAdventureProgress().getCurrentLvl());
            case "minigames", "minigame" ->
                    Comparator.comparingInt(a -> a.getAdventureProgress().getMinigamesWon());
            case "daily", "daily quests" ->
                    Comparator.comparingInt(a -> getCompletedQuestCount(a, QuestType.DAILY));
            case "non-daily", "other quests", "epic", "challenge" ->
                    Comparator.comparingInt(a -> getCompletedQuestCount(a, QuestType.CHALLENGE)
                            + getCompletedQuestCount(a, QuestType.EPIC));
            case "quests", "quest" ->
                    Comparator.comparingInt(a -> getCompletedQuestCount(a, QuestType.DAILY)
                            + getCompletedQuestCount(a, QuestType.CHALLENGE) +
                            getCompletedQuestCount(a, QuestType.EPIC));
            default ->
                    Comparator.comparingInt(a -> a.getScoreRecord().getScore());
        };
    }
}