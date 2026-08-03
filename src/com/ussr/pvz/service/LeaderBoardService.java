package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.dto.LeaderBoardSortRequest;
import com.ussr.pvz.model.leaderboard.LeaderboardColumn;
import com.ussr.pvz.model.leaderboard.LeaderboardEntry;
import com.ussr.pvz.model.quest.ConfigurableQuest;
import com.ussr.pvz.model.quest.QuestType;

import java.util.ArrayList;
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

    public List<LeaderboardEntry> getEntries(
            LeaderboardColumn column,
            boolean ascending
    ) {
        List<Account> accounts =
                new ArrayList<>(App.getAccounts());

        Comparator<Account> comparator =
                comparatorFor(column);

        if (!ascending) {
            comparator = comparator.reversed();
        }

        accounts.sort(comparator.thenComparing(
                Account::getName,
                String.CASE_INSENSITIVE_ORDER
        ));

        return accounts.stream()
                .map(this::toEntry)
                .toList();
    }

    private LeaderboardEntry toEntry(Account account) {
        int daily = getCompletedQuestCount(
                account,
                QuestType.DAILY
        );

        int other = getCompletedQuestCount(
                account,
                QuestType.CHALLENGE
        ) + getCompletedQuestCount(
                account,
                QuestType.EPIC
        );

        return new LeaderboardEntry(
                account.getName(),
                account.getAdventureProgress().getCurrentChapter(),
                account.getAdventureProgress().getCurrentLvl(),
                account.getAdventureProgress().getMinigamesWon(),
                daily,
                other,
                account.getScoreRecord().getScore()
        );
    }

    private Comparator<Account> comparatorFor(
            LeaderboardColumn column
    ) {
        return switch (column) {
            case USERNAME -> Comparator.comparing(
                    Account::getName,
                    String.CASE_INSENSITIVE_ORDER
            );

            case PROGRESS -> Comparator
                    .comparingInt((Account account) ->
                            account.getAdventureProgress()
                                    .getCurrentChapter())
                    .thenComparingInt(account ->
                            account.getAdventureProgress()
                                    .getCurrentLvl());

            case MINIGAMES -> Comparator.comparingInt(
                    account -> account.getAdventureProgress()
                            .getMinigamesWon()
            );

            case DAILY_QUESTS -> Comparator.comparingInt(
                    account -> getCompletedQuestCount(
                            account,
                            QuestType.DAILY
                    )
            );

            case OTHER_QUESTS -> Comparator.comparingInt(
                    account -> getCompletedQuestCount(
                            account,
                            QuestType.CHALLENGE
                    ) + getCompletedQuestCount(
                            account,
                            QuestType.EPIC
                    )
            );

            case SCORE -> Comparator.comparingInt(
                    account -> account.getScoreRecord().getScore()
            );
        };
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