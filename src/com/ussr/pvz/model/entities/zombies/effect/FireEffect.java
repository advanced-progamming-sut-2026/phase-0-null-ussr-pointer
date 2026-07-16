package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.board.Cell;

public class FireEffect implements EffectStatus {
    private boolean isLit = true;
    private final double reach;

    public FireEffect(double reach) {
        this.reach = reach;
    }

    public boolean isLit() { return isLit; }
    public void setLit(boolean lit) { this.isLit = lit; }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive() || !isLit) return;

        int zRow = (int) zombie.getPosition().y();
        double zCol = zombie.getPosition().x();
        int checkCol = (int) Math.floor(zCol);
        int cellInFront = checkCol - 1;

        if (zombie.getFaction() == Faction.ZOMBIES) {
            if (cellInFront >= 0 && cellInFront < session.getLawn().getCols()) {
                checkAndBurnPlant(zombie, session, zRow, cellInFront, zCol);
            }
            if (checkCol >= 0 && checkCol < session.getLawn().getCols()) {
                checkAndBurnPlant(zombie, session, zRow, checkCol, zCol);
            }
        } else {
            session.getZombies().stream()
                    .filter(z -> z.isAlive() && z.getFaction() == Faction.ZOMBIES && (int) z.getPosition().y() == zRow)
                    .forEach(z -> {
                        double distance = z.getPosition().x() - zCol;
                        if (distance >= 0 && distance <= reach) {
                            z.takeDamage(z.getHp());
                        }
                    });
        }
    }

    private void checkAndBurnPlant(Zombie zombie, GameSession session, int row, int col, double zColDouble) {
        Cell cell = session.getLawn().getCell(row, col);
        if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
            Plant targetPlant = cell.getPlant();
            double distance = zColDouble - targetPlant.getLocation().x();
            if (distance >= 0 && distance <= reach) {
                String plantName = targetPlant.getName();

                targetPlant.takeDamage(targetPlant.getHp(), zombie);

                if (!targetPlant.isAlive() && session != null) {
                    session.getEventBus().publish(new GameEvent.PlantIncinerated(
                            plantName, zombie.getAlias(), row, col
                    ));
                }
            }
        }
    }
}