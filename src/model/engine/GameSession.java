package model.engine;

import model.board.Lawn;
import model.entities.items.GroundItem;
import model.entities.zombies.Zombie;
import model.level.Level;
import model.state.inputcmd.InputEvent;
import model.state.ResourceState;

import java.util.List;

public class GameSession {
    private GameClock clock;
    private Level level;
    private ResourceState resourceState;
    private List<Zombie> zombies;
    private List<GroundItem> items;
    private int sunCount;
    private Lawn lawn;

    public void tick() {
    }
}

