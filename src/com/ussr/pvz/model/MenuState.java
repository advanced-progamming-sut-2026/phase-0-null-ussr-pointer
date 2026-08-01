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
    COLLECTION("collection"),
    LEVEL_SELECTION("Level Selection Menu"),
    SHOP("shop");

    private String name;

    MenuState(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean showsGlobalHud() {
        return switch (this) {
            case LOGIN, REGISTER, GAME -> false;
            default -> true;
        };
    }

    public MenuState getParent() {
        return switch (this) {
            case LOGIN -> REGISTER;
            case GAME, SETTING, NETWORK, NEWS, PROFILE -> MAIN;
            case COLLECTION, GREENHOUSE, LEADERBOARD, TRAVEL_LOG,
                 CHOOSE_PLANT, LEVEL_SELECTION -> GAME;
            case SHOP -> GREENHOUSE;
            case REGISTER, MAIN -> null;
        };
    }
}
