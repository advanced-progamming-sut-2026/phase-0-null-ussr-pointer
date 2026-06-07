package model.board.structures;

import model.engine.GameSession;

public abstract class InteractableStructure extends model.engine.GameEntity {
    private boolean destroyed;
    private int hp;

    public void takeDamage(int damage) {}
    public abstract void onDestroy(GameSession session);
    @Override public void tick() {}

}

