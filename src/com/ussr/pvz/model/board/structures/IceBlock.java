package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;

public class IceBlock extends InteractableStructure implements Damageable {
    private final Plant boundPlant;
    private int hp;

    public IceBlock(Plant boundPlant, int initialHp) {
        this.boundPlant = boundPlant;
        this.hp = initialHp;
        this.setAlive(true);
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
            boundPlant.setChillLevel(0);

            int col = (int) this.getPosition().x();
            int row = (int) this.getPosition().y();
            Cell cell = session.getLawn().getCell(row, col);

            if (cell != null) {
                cell.setPlant(boundPlant);
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