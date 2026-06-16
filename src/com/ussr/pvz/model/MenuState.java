package com.ussr.pvz.model;

public enum MenuState {
    MAIN("main"),
    GAME("game"),
    LOGIN("login"),
    REGISTER("register"),
    GREENHOUSE("green house"),
    LEADERBOARD("leaderboard"),
    NETWORK("network"),
    NEWS("news"),
    PROFILE("profile"),
    SETTING("setting"),
    TRAVEL_LOG("travel log"),
    CHOOSE_PLANT("choose plant"),
    COLLECTION("collection");

    private String name;

    MenuState(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
