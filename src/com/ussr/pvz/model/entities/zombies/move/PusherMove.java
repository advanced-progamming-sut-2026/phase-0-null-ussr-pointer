package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.board.Row;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.PushableStructure;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.util.Vec2;
import java.util.ArrayList;
import java.util.List;

public class PusherMove implements MoveBehavior {
    private static final double PUSH_RANGE = 1.1;

    @Override
    public void move(Zombie zombie, GameSession session) {
        Vec2 pos = zombie.getPosition();
        if (pos == null || session.getLawn() == null) return;

        double deltaX = zombie.getSpeed().x() * GameClock.SECONDS_PER_TICK;
        double targetZombieX = pos.x() + deltaX;
        int currentRow = (int) pos.y();

        Row row = session.getLawn().getRow(currentRow);
        if (row != null) {
            List<CellStructureMapping> itemsToPush = new ArrayList<>();

            for (Cell cell : row.getCells()) {
                var structure = cell.getInteractableStructure();
                if (structure instanceof PushableStructure && structure.isAlive()) {
                    PushableStructure ps = (PushableStructure) structure;
                    double structX = ps.getPosition().x();

                    if (structX < pos.x() && (pos.x() - structX) <= PUSH_RANGE) {
                        itemsToPush.add(new CellStructureMapping(cell, ps));
                    }
                }
            }

            for (CellStructureMapping mapping : itemsToPush) {
                Cell oldCell = mapping.cell;
                PushableStructure targetStructure = mapping.structure;

                double nextStructX = targetStructure.getPosition().x() + deltaX;
                int oldCol = oldCell.getCol();
                int newCol = (int) nextStructX;

                targetStructure.setPosition(Vec2.of(nextStructX, targetStructure.getPosition().y()));

                if (oldCol != newCol && newCol >= 0) {
                    Cell newCell = session.getLawn().getCell(currentRow, newCol);
                    if (newCell != null) {
                        Plant targetPlant = newCell.getPlant();
                        if (targetPlant != null && targetPlant.isAlive()) {
                            targetPlant.takeDamage(targetPlant.getHp());
                        }

                        newCell.setStructure(targetStructure);
                        oldCell.setStructure(null);
                    }
                }
            }
        }

        zombie.setPosition(Vec2.of(targetZombieX, pos.y()));

        if (zombie.getPosition().x() < 0) {
            session.onZombieReachedEnd();
        }
    }

    private record CellStructureMapping(Cell cell, PushableStructure structure) {
    }
}