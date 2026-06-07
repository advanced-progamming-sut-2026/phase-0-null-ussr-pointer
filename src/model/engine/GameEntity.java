package model.engine;

import model.util.Vec2;

public abstract class GameEntity implements Tickable {
    private Vec2 position;
    private Vec2 speed;

    protected boolean isAlive = true;

    @Override public abstract void tick();

    public Vec2 getPosition() {return this.position;}
    public void setPosition(Vec2 target) {this.position = target;}
    public boolean isAlive() {return this.isAlive;}

}

