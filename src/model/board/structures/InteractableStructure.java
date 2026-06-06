package model.board.structures;

import model.engine.GameSession;

public abstract class InteractableStructure extends model.engine.GameEntity {
    private boolean destroyed;

    public abstract void onInteract(GameSession session);
    @Override public void tick() {}

}

