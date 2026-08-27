package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Row;
import com.ussr.pvz.model.board.structures.PushableStructure;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieActivity;
import com.ussr.pvz.model.util.Vec2;

public class IceBlockPusherMove implements MoveBehavior {

    private static final double PUSH_RANGE = 0.5;

    private static final float PUSH_TRIGGER_TIME = 2.0f;

    private boolean pushing = false;
    private float pushElapsed = 0f;
    private boolean pushedThisCycle = false;

    @Override
    public void move(Zombie zombie, GameSession session, float delta) {
        Vec2 pos = zombie.getPosition();
        if (pos == null || session.getLawn() == null) return;

        int currentRow = (int) pos.y();
        Row row = session.getLawn().getRow(currentRow);
        PushableStructure iceBlock = (row != null) ? findNearbyIceBlock(row, zombie, pos) : null;

        if (iceBlock != null) {
            pushing = true;
            zombie.setState(ZombieActivity.PUSHING);
            pushElapsed += delta;

            if (!pushedThisCycle && pushElapsed >= PUSH_TRIGGER_TIME) {
                pushColumn(session, currentRow, iceBlock, zombie);
                pushedThisCycle = true;
            }
            return;
        }

        pushing = false;
        pushElapsed = 0f;
        pushedThisCycle = false;

        double deltaX = zombie.getSpeed().x() * delta;
        zombie.setPosition(Vec2.of(pos.x() + deltaX, pos.y()));
    }

    public boolean isPushing() {
        return pushing;
    }

    private PushableStructure findNearbyIceBlock(Row row, Zombie zombie, Vec2 pos) {
        for (Cell cell : row.getCells()) {
            var structure = cell.getInteractableStructure();
            if (!(structure instanceof PushableStructure ps) || !structure.isAlive()) continue;
            if (ps != zombie.getPushedStructure()) continue;

            double structX = ps.getPosition().x();
            if (structX <= pos.x() && (pos.x() - structX) <= PUSH_RANGE) {
                return ps;
            }
        }
        return null;
    }

    private void pushColumn(GameSession session, int row, PushableStructure iceBlock, Zombie zombie) {
        int oldCol = (int) iceBlock.getPosition().x();
        int newCol = oldCol - 1;
        if (newCol < 0) return;

        Cell oldCell = session.getLawn().getCell(row, oldCol);
        Cell newCell = session.getLawn().getCell(row, newCol);
        if (newCell == null) return;

        Plant targetPlant = newCell.getPlant();
        if (targetPlant != null && targetPlant.isAlive()) {
            targetPlant.takeDamage(targetPlant.getHp(), zombie);
        }

        iceBlock.setPosition(Vec2.of(newCol, iceBlock.getPosition().y()));

        if (oldCell != null) {
            oldCell.setStructure(null);
        }
        newCell.setStructure(iceBlock);
    }
}