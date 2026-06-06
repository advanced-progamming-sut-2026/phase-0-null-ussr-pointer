package model.entities.zombies;

import model.engine.GameEntity;

import java.util.ArrayList;

public class Zombie extends GameEntity {
    private final String name;
    private final ArrayList<Behaivior> behaiviors = new ArrayList<>();
    private int hp;


}
