package model.board.structures;

import model.engine.GameSession;

public class LawnMower extends InteractableStructure {
    private boolean activated;
    private int lane;


    @Override
    public void onDestroy(GameSession session) {

    }

    @Override public void tick() {}

}