package com.ussr.pvz.model.level;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LevelLoader {
    private static final String DEFAULT_PATH = "resources/levels.json";
    private final Gson gson = new Gson();

    public JsonContainer.JsonWorldData load() {
        return load(DEFAULT_PATH);
    }

    public JsonContainer.JsonWorldData load(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("Critical Error: levels.json not found at " + path);
            return null;
        }
        try (FileReader reader = new FileReader(file)) {
            JsonContainer.JsonWorldData world = gson.fromJson(reader, JsonContainer.JsonWorldData.class);
            validate(world);
            return world;
        } catch (IOException e) {
            System.err.println("Critical Error: could not read levels.json: " + e.getMessage());
            return null;
        }
    }

    private void validate(JsonContainer.JsonWorldData world) {
        if (world == null || world.chapters == null) {
            throw new IllegalStateException("levels.json is empty or malformed");
        }
        for (JsonContainer.JsonChapterData chapter : world.chapters) {
            if (chapter.id == null || chapter.id.isBlank())
                throw new IllegalStateException("chapter missing id");
            if (chapter.levels == null || chapter.levels.isEmpty())
                throw new IllegalStateException("chapter " + chapter.id + " has no levels");
            for (JsonContainer.JsonLevelData level : chapter.levels) {
                if (level.id == null || level.id.isBlank())
                    throw new IllegalStateException("level missing id in chapter " + chapter.id);
            }
        }
    }
}