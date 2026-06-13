package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.state.ResourceState;

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

