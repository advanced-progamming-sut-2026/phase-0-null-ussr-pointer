package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;

public class Grave extends InteractableStructure implements Damageable {

    public enum Content { NONE, SUN, PLANT_FOOD }

    private final String zombieId; // Used if a zombie spawns from this grave later
    private final Content content;
    private int hp;

    public Grave(String zombieId, Content content) {
        this.zombieId = zombieId;
        this.content = content != null ? content : Content.NONE;
        this.hp = 700; // Requirement from assignment sheet
        this.setAlive(true);
    }

    public Grave(String zombieId) {
        this(zombieId, Content.NONE);
    }

    public Grave() {
        this(null, Content.NONE);
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
        session.getLawn().getCell(row , col).getTile().setType(TileType.Normal);

        switch (content) {
            case SUN -> {
                session.addSun(50);
                System.out.println("The tombstone crumbled and released 50 sun!");
            }
            case PLANT_FOOD -> {
                session.addPlantFood();
                System.out.println("The tombstone crumbled and released a plant food!");
            }
            case NONE -> {}
        }
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

    public Content getContent() {
        return content;
    }
}