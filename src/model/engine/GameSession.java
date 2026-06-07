package model.engine;

import model.entities.items.GroundItem;
import model.entities.zombies.Zombie;
import model.level.Level;
import model.state.InputEvent;
import model.state.ResourceState;

import java.util.List;

public class GameSession {
    private GameClock clock;
    private Level level;
    private ResourceState resourceState;
    private List<Zombie> zombies;
    private List<GroundItem> items;

    public void handleInput(InputEvent inputEvent) {}
    public void tick() {}
}

