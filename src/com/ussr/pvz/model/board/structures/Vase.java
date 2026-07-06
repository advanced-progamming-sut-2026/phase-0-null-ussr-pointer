package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;

public class Vase extends InteractableStructure implements Damageable {
    private int hp = 200; //temp
    private VaseType type;
    private Plant containedPlant;
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
            case PLANT ->
                session.getItems().add()
                break;
            case NORMAL ->
                break;
            case GARGANTAUR ->
                break;
            case null, default ->
                break;
        }
    }

    @Override
    public void tick() {
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

    public Plant getContainedPlant() {
        return containedPlant;
    }

    public void setContainedPlant(Plant containedPlant) {
        this.containedPlant = containedPlant;
    }

    public Zombie getContainedZombie() {
        return containedZombie;
    }

    public void setContainedZombie(Zombie containedZombie) {
        this.containedZombie = containedZombie;
    }
}