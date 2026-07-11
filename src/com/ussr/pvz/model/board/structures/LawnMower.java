package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.util.Vec2;

public class LawnMower extends InteractableStructure {
    private boolean activated = false;
    private final int lane;
    private static final double DESPAWN_X = 9.5;

    public LawnMower(int lane, Vec2 startingPosition) {
        this.lane = lane;
        this.setPosition(startingPosition);
        this.setSpeed(Vec2.zero());
    }

    public void activate() {
        if (!this.activated) {
            this.activated = true;
            this.setSpeed(new Vec2(0.15, 0));
        }
    }

    public boolean isActivated() {
        return activated;
    }

    public int getLane() {
        return lane;
    }

    @Override
    public void onDestroy(GameSession session) {
        this.setAlive(false);
    }

    @Override
    public void tick() {
        if (!isAlive()) return;
        if (!activated) return;

        GameSession session = App.getGameSession();
        if (session == null) return;

        this.setPosition(this.getPosition().add(this.getSpeed()));

        if (this.getPosition().x() > DESPAWN_X) {
            this.setAlive(false);
            return;
        }

        double mowerX = this.getPosition().x();

        session.getZombies().forEach(zombie -> {
            int zombieLane = (int) zombie.getPosition().y();

            if (zombieLane == this.lane && zombie.isAlive()) {
                if (zombie.getPosition().x() <= mowerX) {
                    if (!zombie.getName().toLowerCase().contains("boss")) {
                        zombie.takeDamage(zombie.getHp() + 1000);
                    }
                }
            }
        });
    }

    @Override
    public void takeDamage(int damage) {

    }
}