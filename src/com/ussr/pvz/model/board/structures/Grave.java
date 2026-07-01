package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;

public class Grave extends InteractableStructure implements Damageable {
    private final String zombieId; // Used if a zombie spawns from this grave later
    private int hp;

    public Grave(String zombieId) {
        this.zombieId = zombieId;
        this.hp = 700; // Requirement from assignment sheet
        this.setAlive(true);
    }

    public Grave() {
        this(null);
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
        int row = (int) this.getPosition().y();
        int col = (int) this.getPosition().x();
        session.notifyGraveDestroyed(row, col);
    }

    @Override
    public void tick() {
    }

    public int getHp() {
        return this.hp;
    }

    public String getZombieId() {
        return zombieId;
    }
}