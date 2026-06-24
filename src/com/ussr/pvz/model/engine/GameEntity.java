package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.util.Vec2;

public abstract class GameEntity implements Tickable {
    private Vec2 position;
    private Vec2 speed;

    protected boolean isAlive = true;

    @Override
    public abstract void tick();

    public Vec2 getPosition() {
        return this.position;
    }

    public void setPosition(Vec2 target) {
        this.position = target;
    }

    public Vec2 getSpeed() {
        return this.speed;
    }

    public void setSpeed(Vec2 speed) {
        this.speed = speed;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }
}