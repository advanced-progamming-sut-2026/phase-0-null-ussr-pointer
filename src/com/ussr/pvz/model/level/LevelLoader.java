package com.ussr.pvz.model.level;

import com.google.gson.Gson;

public class LevelLoader {
    private static final String DEFAULT_PATH = "data/levels.json";
    private final Gson gson = new Gson();

    public JsonContainer.JsonWorldData load() {
        return load(DEFAULT_PATH);
    }

    public JsonContainer.JsonWorldData load(String path) {
        return null;
    }

    private void validate(JsonContainer.JsonWorldData world) {
    }
}
