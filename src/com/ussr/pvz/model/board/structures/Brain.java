package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;

public class Brain extends InteractableStructure implements Damageable {
    private int hp = 100; // Eaten relatively quickly once reached

    public Brain() {
        this.setAlive(true);
    }

    @Override
    public void takeDamage(int damage) {
        if (!isAlive()) return;
        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            this.setAlive(false);
        }
    }

    @Override
    public void onDestroy(GameSession session) {
        // Handled by the behavior to check win conditions
    }

    @Override
    public void tick() {
        // Static target, does nothing
    }

    public int getHp() {
        return hp;
    }
}