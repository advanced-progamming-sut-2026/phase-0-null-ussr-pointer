package model.board.structures;

import model.engine.GameSession;

public class Container extends InteractableStructure{
    private int hp;
    private String containedZombieId;

    @Override
    public void onInteract(GameSession session) {

    }
    @Override public void tick() {}

}
