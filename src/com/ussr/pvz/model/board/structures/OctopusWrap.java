package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.board.Cell;
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
        if (boundPlant != null && boundPlant.isAlive()) {
            boundPlant.setState(Plant.PlantState.ACTIVE);

            int targetRow = (int) this.getPosition().y();
            int targetCol = (int) this.getPosition().x();

            Cell targetCell = session.getLawn().getCell(targetRow, targetCol);

            if (targetCell != null) {
                targetCell.setPlant(boundPlant);
            }
            session.getPlants().add(boundPlant);
        }
    }

    @Override
    public void tick() {
        if (boundPlant == null || !boundPlant.isAlive()) {
            this.setAlive(false);
        }
    }
}