package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.BurningGround;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;
import com.ussr.pvz.model.util.Vec2;

public class RowIgniteMove implements ZombossMove {
    private static final double BURN_DURATION = 4.0;

    @Override
    public void execute(ZombossController controller, GameSession session) {
        for (int row : controller.getOccupiedRows()) {
            if (row >= session.getLawn().getRows()) continue;
            for (int col = 0; col < session.getLawn().getCols(); col++) {
                session.removePlantAt(col, row);

                Cell cell = session.getLawn().getCell(row, col);
                if (cell != null && cell.getTile() != null) {
                    cell.getTile().setType(TileType.Burning);

                    BurningGround burningGround = new BurningGround(BURN_DURATION);
                    burningGround.setPosition(Vec2.of(col, row));
                    cell.setStructure(burningGround);
                    session.registerStructure(burningGround);
                }
            }
        }
    }
}