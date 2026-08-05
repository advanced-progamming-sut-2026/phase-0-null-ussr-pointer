package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.session.GameSession;

public class Brain extends InteractableStructure implements Damageable {
    private int hp = 100; // Eaten relatively quickly once reached
    private final String pamLocation = "768/FULL/ZOMBIE/POWER_BRAIN_PROJECTILE/POWER_BRAIN_PROJECTILE.PAM";
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
    public void update(float delta) {
        // Static target, does nothing
    }

    public int getHp() {
        return hp;
    }

    public String getPamLocation() {
        return pamLocation;
    }

}