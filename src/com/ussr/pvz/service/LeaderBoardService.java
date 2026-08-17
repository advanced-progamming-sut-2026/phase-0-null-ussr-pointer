package com.ussr.pvz.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ussr.pvz.model.dto.LeaderBoardSortRequest;
import com.ussr.pvz.model.leaderboard.LeaderboardColumn;
import com.ussr.pvz.model.leaderboard.LeaderboardEntry;
import com.ussr.pvz.model.util.SessionManager;
import com.ussr.pvz.network.NetworkClient;
import com.ussr.pvz.shared.dto.LeaderboardEntryDto;
import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;
import com.ussr.pvz.shared.network.RequestType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderBoardService {

    private final NetworkClient networkClient;
    private final Gson gson = new Gson();

    public LeaderBoardService() {
        this.networkClient = NetworkClient.getInstance();
    }


    public String show() {

        List<LeaderboardEntry> entries =
                getEntries(
                        LeaderboardColumn.SCORE,
                        false
                );

        if (entries.isEmpty()) {
            return "No accounts found.";
        }

        return formatEntries(entries);
    }


    public List<LeaderboardEntry> getEntries(
            LeaderboardColumn column,
            boolean ascending
    ) {

        List<LeaderboardEntry> entries =
                loadEntriesFromServer();

        Comparator<LeaderboardEntry> comparator =
                comparatorFor(column);

        if (!ascending) {
            comparator = comparator.reversed();
        }

        entries.sort(
                comparator.thenComparing(
                        LeaderboardEntry::username,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return entries;
    }


    public String sort(
            LeaderBoardSortRequest request
    ) {

        String rawColumn =
                request.column() != null
                        ? request.column()
                        .toLowerCase()
                        .trim()
                        : "score";

        String order =
                request.order() != null
                        ? request.order()
                        .toLowerCase()
                        .trim()
                        : "desc";

        boolean ascending =
                order.equals("asc")
                        || order.equals("ascending")
                        || order.equals("+");

        LeaderboardColumn column =
                mapColumn(rawColumn);

        List<LeaderboardEntry> entries =
                getEntries(
                        column,
                        ascending
                );

        StringBuilder result =
                new StringBuilder();

        result.append(
                "Leaderboard successfully sorted by '"
        );

        result.append(
                rawColumn
        );

        result.append(
                "' in "
        );

        result.append(
                ascending
                        ? "ascending"
                        : "descending"
        );

        result.append(
                " order.\n\n"
        );

        if (entries.isEmpty()) {

            result.append(
                    "No accounts found."
            );

            return result.toString();
        }

        result.append(
                formatEntries(entries)
        );

        return result.toString();
    }


    // =========================================================
    // SERVER DATA
    // =========================================================

    private List<LeaderboardEntry>
    loadEntriesFromServer() {

        String token =
                SessionManager.getToken();

        if (token == null ||
                token.isBlank()) {

            return new ArrayList<>();
        }

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.GET_LEADERBOARD,
                        token,
                        null
                );

        NetworkResponse response;

        try {

            response =
                    networkClient.send(
                            request
                    );

        } catch (Exception e) {

            System.err.println(
                    "Leaderboard network error: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }

        if (response == null ||
                !response.isSuccess() ||
                response.getData() == null ||
                !response.getData()
                        .has("entries")) {

            return new ArrayList<>();
        }

        JsonArray array =
                response
                        .getData()
                        .getAsJsonArray(
                                "entries"
                        );

        List<LeaderboardEntry> entries =
                new ArrayList<>();

        for (JsonElement element : array) {

            LeaderboardEntryDto dto =
                    gson.fromJson(
                            element,
                            LeaderboardEntryDto.class
                    );

            LeaderboardEntry entry =
                    new LeaderboardEntry(
                            dto.username(),
                            dto.currentChapter(),
                            dto.currentLevel(),
                            dto.minigamesWon(),
                            dto.dailyQuests(),
                            dto.otherQuests(),
                            dto.score()
                    );

            entries.add(entry);
        }

        return entries;
    }


    // =========================================================
    // SORTING
    // =========================================================

    private Comparator<LeaderboardEntry>
    comparatorFor(
            LeaderboardColumn column
    ) {

        return switch (column) {

            case USERNAME ->
                    Comparator.comparing(
                            LeaderboardEntry::username,
                            String.CASE_INSENSITIVE_ORDER
                    );

            case PROGRESS ->
                    Comparator
                            .comparingInt(
                                    LeaderboardEntry::chapter
                            )
                            .thenComparingInt(
                                    LeaderboardEntry::level
                            );

            case MINIGAMES ->
                    Comparator.comparingInt(
                            LeaderboardEntry::minigames
                    );

            case DAILY_QUESTS ->
                    Comparator.comparingInt(
                            LeaderboardEntry::dailyQuests
                    );

            case OTHER_QUESTS ->
                    Comparator.comparingInt(
                            LeaderboardEntry::otherQuests
                    );

            case SCORE ->
                    Comparator.comparingInt(
                            LeaderboardEntry::score
                    );
        };
    }


    private LeaderboardColumn mapColumn(
            String column
    ) {

        return switch (column) {

            case "username" ->
                    LeaderboardColumn.USERNAME;

            case "progress",
                 "level",
                 "chapter" ->
                    LeaderboardColumn.PROGRESS;

            case "minigames",
                 "minigame" ->
                    LeaderboardColumn.MINIGAMES;

            case "daily",
                 "daily quests" ->
                    LeaderboardColumn.DAILY_QUESTS;

            case "non-daily",
                 "other quests",
                 "epic",
                 "challenge",
                 "quests",
                 "quest" ->
                    LeaderboardColumn.OTHER_QUESTS;

            default ->
                    LeaderboardColumn.SCORE;
        };
    }


    // =========================================================
    // DISPLAY
    // =========================================================

    private String formatEntries(
            List<LeaderboardEntry> entries
    ) {

        StringBuilder sb =
                new StringBuilder();

        sb.append(String.format(
                "%-15s | %-15s | %-10s | %-12s | %-12s | %-10s\n",
                "Username",
                "Progress",
                "Minigames",
                "Daily Quests",
                "Other Quests",
                "MooPoints"
        ));

        sb.append(
                "-".repeat(85)
        ).append("\n");

        for (LeaderboardEntry entry : entries) {

            String progress =
                    "Ch:"
                            + entry.chapter()
                            + " Lv:"
                            + entry.level();

            sb.append(String.format(
                    "%-15s | %-15s | %-10d | %-12d | %-12d | %-10d\n",
                    entry.username(),
                    progress,
                    entry.minigames(),
                    entry.dailyQuests(),
                    entry.otherQuests(),
                    entry.score()
            ));
        }

        return sb.toString();
    }
}