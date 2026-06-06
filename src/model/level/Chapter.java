package model.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Chapter {
    private final String id;
    private final String name;
    private final GameMode gameMode;
    private List<String> allowedPlants;
    private final List<Level> levels;

    public Chapter(String id, String name, GameMode gameMode) {
        this.id = id;
        this.name = name;
        this.gameMode = gameMode;
        this.levels = new ArrayList<>();
    }

    public void addLevel(Level level) {
        levels.add(level);
    }

    public Optional<Level> findLevel(String id) {
        return levels.stream().filter(l -> l.getId().equals(id)).findFirst();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public List<String> getAllowedPlants() {
        return allowedPlants;
    }

    public void setAllowedPlants(List<String> p) {
        this.allowedPlants = p;
    }

    public List<Level> getLevels() {
        return Collections.unmodifiableList(levels);
    }
}
