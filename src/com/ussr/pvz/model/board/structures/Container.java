package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;

public class Container extends InteractableStructure implements Damageable {
    private int hp = 500; //temp
    private String containedZombieId;

    @Override
    public void takeDamage(int damage) {
        if (!isAlive()) return;

        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            setAlive(false);
        }
    }

    @Override
    public void onDestroy(GameSession session) {
    }

    @Override
    public void tick() {
    }

    public int getHp() {
        return hp;
    }

    public String getContainedZombieId() {
        return containedZombieId;
    }

    public void setContainedZombieId(String containedZombieId) {
        this.containedZombieId = containedZombieId;
    }
}