package com.ussr.pvz.model.leaderboard;

public enum SortDirection {
    ASCENDING("Ascending"),
    DESCENDING("Descending");

    private final String title;

    SortDirection(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}