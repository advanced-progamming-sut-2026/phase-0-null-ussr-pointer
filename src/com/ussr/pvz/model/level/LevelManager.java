package com.ussr.pvz.model.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LevelManager {
    private List<Chapter> chapters = new ArrayList<>();
    private Chapter currentChapter;
    private Level currentLevel;

    public void loadFromJson() {

    }

    public void startChapter(String id) {
        currentChapter = chapters.stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
        currentLevel = currentChapter.getLevels().getFirst();
    }

    public void startLevel(String id) {
        currentLevel = currentChapter.findLevel(id).orElseThrow();
        currentLevel.onStart();
    }

    public void nextLevel() {
    }

    public void completeCurrentLevel() {
        currentLevel.onComplete();
    }

    public Chapter getCurrentChapter() {
        return currentChapter;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public List<Chapter> getChapters() {
        return Collections.unmodifiableList(chapters);
    }
}

