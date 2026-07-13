package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.dto.LeaderBoardSortRequest;

import java.util.Comparator;
import java.util.List;

// TODO(leaderboard-verification): do an end-to-end pass confirming the sortable columns exactly
//  match spec (last chapter/level reached, minigames won, quests completed — daily vs. non-daily
//  counted SEPARATELY per spec, highest MooPoint score) and that both ascending and descending
//  work for every column, including tie-breaking and an empty-account-list case.
public class LeaderBoardService {

    public LeaderBoardService() {
    }

    public String show() {
        List<Account> accounts = App.getAccounts();
        if (accounts.isEmpty()) {
            return "No accounts found.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-15s | %-15s | %-10s | %-10s | %-10s\n",
                "Username", "Progress", "Minigames", "Quests", "Score"));
        sb.repeat("-", 70).append("\n");

        for (Account acc : accounts) {
            String progress = "Ch:" + acc.getAdventureProgress().getCurrentChapter() +
                    " Lv:" + acc.getAdventureProgress().getCurrentLvl();
            int minigames = acc.getAdventureProgress().getMinigamesWon();
            int quests = acc.getAdventureProgress().getQuestsCompleted();
            int score = acc.getScoreRecord().getScore();

            sb.append(String.format("%-15s | %-15s | %-10d | %-10d | %-10d\n",
                    acc.getName(), progress, minigames, quests, score));
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

        comparator = comparator.thenComparing(Account::getName);
        App.getAccounts().sort(comparator);

        return "Leaderboard successfully sorted by '" + rawCol + "' in " +
                (isAsc ? "ascending" : "descending") + " order.\n\n" + show();
    }

    private Comparator<Account> getComparatorForColumn(String column) {
        return switch (column) {
            case "progress", "level", "chapter" ->
                    Comparator.comparingInt((Account a) -> a.getAdventureProgress().getCurrentChapter())
                            .thenComparingInt(a -> a.getAdventureProgress().getCurrentLvl());
            case "minigames", "minigame" ->
                    Comparator.comparingInt(a -> a.getAdventureProgress().getMinigamesWon());
            case "quests", "quest" ->
                    Comparator.comparingInt(a -> a.getAdventureProgress().getQuestsCompleted());
            default -> Comparator.comparingInt(a -> a.getScoreRecord().getScore());
        };
    }
}
