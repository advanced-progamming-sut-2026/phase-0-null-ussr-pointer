package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.dto.LeaderBoardSortRequest;

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
        String col = request.column() != null ? request.column().toLowerCase().trim() : "score";
        String ord = request.order() != null ? request.order().toLowerCase().trim() : "desc";

        boolean isAsc = ord.equals("asc") || ord.equals("ascending") || ord.equals("+");

        Comparator<Account> comparator;

        switch (col) {
            case "progress":
            case "level":
            case "chapter":
                comparator = Comparator.comparingInt((Account a) -> a.getAdventureProgress().getCurrentChapter())
                        .thenComparingInt(a -> a.getAdventureProgress().getCurrentLvl());
                break;
            case "minigames":
            case "minigame":
                comparator = Comparator.comparingInt(a -> a.getAdventureProgress().getMinigamesWon());
                break;
            case "quests":
            case "quest":
                comparator = Comparator.comparingInt(a -> a.getAdventureProgress().getQuestsCompleted());
                break;
            case "score":
            default:
                comparator = Comparator.comparingInt(a -> a.getScoreRecord().getScore());
                col = "score";
                break;
        }

        if (!isAsc) {
            comparator = comparator.reversed();
        }

        comparator = comparator.thenComparing(Account::getName);

        App.getAccounts().sort(comparator);

        return "Leaderboard successfully sorted by '" + col + "' in " + (isAsc ? "ascending" : "descending") + " order.\n\n" + show();
    }
}