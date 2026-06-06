package model.engine;

public abstract class GameEntity implements Tickable {
    protected double x;
    protected double y;
    protected boolean isAlive = true;

    @Override public abstract void tick();
}

