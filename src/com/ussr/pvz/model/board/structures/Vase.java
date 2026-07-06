package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.SeedPackDrop;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;

import java.util.Random;

public class Vase extends InteractableStructure implements Damageable {
    private int hp = 200; //temp
    private VaseType type;
    private SeedPackDrop seedPackDrop;
    private Zombie containedZombie;

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
        switch (type) {
            case PLANT -> session.getItems().add(seedPackDrop);
            case NORMAL -> {
                Random rand = new Random();
                rand.setSeed(System.currentTimeMillis());
                if (rand.nextInt() % 2 == 0) {
                    session.getItems().add(seedPackDrop);
                } else {
                    session.getZombies().add(containedZombie);
                }
            }
            case GARGANTAUR -> {
                session.getZombies().add(containedZombie);
            }
        }
        this.isAlive = false;
    }

    public int getHp() {
        return hp;
    }

    public VaseType getType() {
        return type;
    }

    public void setType(VaseType type) {
        this.type = type;
    }

    public SeedPackDrop getContainedPlant() {
        return seedPackDrop;
    }

    public void setSeedPackDrop(SeedPackDrop containedPlant) {
        this.seedPackDrop = containedPlant;
    }

    public Zombie getContainedZombie() {
        return containedZombie;
    }

    public void setContainedZombie(Zombie containedZombie) {
        this.containedZombie = containedZombie;
    }
}