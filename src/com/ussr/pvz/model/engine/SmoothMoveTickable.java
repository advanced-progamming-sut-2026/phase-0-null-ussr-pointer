package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.util.Vec2;

public class SmoothMoveTickable implements Tickable {
    private final GameEntity entity;
    private final Vec2 start;
    private final Vec2 target;
    private final double duration;
    private double elapsed = 0;
    private boolean finished = false;

    public SmoothMoveTickable(GameEntity entity, Vec2 target, double duration) {
        this.entity = entity;
        this.start = entity.getPosition();
        this.target = target;
        this.duration = Math.max(0.01, duration);
    }

    @Override
    public void update(float delta) {
        if (finished) return;
        if (!entity.isAlive()) {
            finished = true;
            return;
        }

        elapsed += delta;
        double t = Math.min(1.0, elapsed / duration);
        double eased = 1 - Math.pow(1 - t, 3);

        double x = start.x() + (target.x() - start.x()) * eased;
        double y = start.y() + (target.y() - start.y()) * eased;
        entity.setPosition(Vec2.of(x, y));

        if (t >= 1.0) {
            finished = true;
        }
    }

    public boolean isFinished() {
        return finished;
    }
}