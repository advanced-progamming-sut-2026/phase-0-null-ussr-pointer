package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;

public class OctopusWrap extends InteractableStructure implements Damageable {
    private final Plant boundPlant;
    private int hp;

    public OctopusWrap(Plant boundPlant, int initialHp) {
        this.boundPlant = boundPlant;
        this.hp = initialHp;
        this.setAlive(true);
        // Automatically disable the plant when applied
        this.boundPlant.setState(Plant.PlantState.INCAPACITATED);
    }

    @Override
    public void takeDamage(int damage) {
        if (!isAlive()) return;
        hp -= damage;
        if (hp <= 0) {
            setAlive(false);
        }
    }

    @Override
    public void onDestroy(GameSession session) {
        // Free the plant when the octopus is destroyed
        if (boundPlant != null && boundPlant.isAlive()) {
            boundPlant.setState(Plant.PlantState.ACTIVE);
        }
    }

    @Override
    public void tick() {
        // If the underlying plant dies (e.g., crushed by a Gargantuar), the wrap should also vanish
        if (boundPlant == null || !boundPlant.isAlive()) {
            this.setAlive(false);
        }
    }
}