package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.board.Cell;

public class FireEffect implements EffectStatus {
    private boolean isLit = true;
    private final double reach;

    // For the Explorer Zombie, reach is 1.0 (less than one cell in front)
    public FireEffect(double reach) {
        this.reach = reach;
    }

    public boolean isLit() {
        return isLit;
    }

    public void setLit(boolean lit) {
        this.isLit = lit;
    }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive() || !isLit) return;

        int zRow = (int) zombie.getPosition().y();
        double zCol = zombie.getPosition().x();

        int checkCol = (int) Math.floor(zCol);
        int cellInFront = checkCol - 1;

        if (cellInFront >= 0 && cellInFront < session.getLawn().getCols()) {
            checkAndBurnCell(zombie, session, zRow, cellInFront, zCol);
        }

        if (checkCol >= 0 && checkCol < session.getLawn().getCols()) {
            checkAndBurnCell(zombie, session, zRow, checkCol, zCol);
        }
    }

    private void checkAndBurnCell(Zombie zombie, GameSession session, int row, int col, double zColDouble) {
        Cell cell = session.getLawn().getCell(row, col);

        if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
            Plant targetPlant = cell.getPlant();

            int pCol = targetPlant.getLocation().x();

            double distance = zColDouble - pCol;
            if (distance >= 0 && distance <= reach) {
                targetPlant.takeDamage(targetPlant.getHp());
            }
        }
    }
}