package model.board.structures;

import model.engine.GameSession;

public class Grave extends InteractableStructure {
    private String zombieId;
    private boolean cleared;

    @Override
    public void onInteract(GameSession session) {

    }

    @Override public void tick() {}

}