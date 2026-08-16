package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.GlacierBlock;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.projectiles.IceRocketProjectile;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;
import com.ussr.pvz.model.util.Vec2;

import java.util.Random;

public class IceRocketMove implements ZombossMove {
    private static final int EXCLUDED_RIGHT_COLUMNS = 3;
    private static final int MAX_ATTEMPTS = 30;

    @Override
    public void execute(ZombossController controller, GameSession session) {
        Random random = new Random();

        int maxCol = Math.max(1, session.getLawn().getCols() - EXCLUDED_RIGHT_COLUMNS);

        int row = 0;
        int col = 0;
        boolean found = false;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            row = random.nextInt(session.getLawn().getRows());
            col = random.nextInt(maxCol);

            if (!hasGlacierBlock(session, row, col)) {
                found = true;
                break;
            }
        }

        // Fallback: if every rolled tile had ice on it, do one final scan for
        // any valid tile without a glacier block instead of just giving up.
        if (!found) {
            int[] fallback = findAnyFreeTile(session, maxCol);
            if (fallback != null) {
                row = fallback[0];
                col = fallback[1];
            }
            // If truly every tile is frozen, we just fire at the last rolled tile.
        }

        Vec2 startPos = controller.getPrimary().getPosition();
        IceRocketProjectile projectile = new IceRocketProjectile(startPos, Vec2.of(col, row), row, col);
        session.addZombieProjectile(projectile);
    }

    private boolean hasGlacierBlock(GameSession session, int row, int col) {
        Cell cell = session.getLawn().getCell(row, col);
        return cell != null
                && cell.getInteractableStructure() instanceof GlacierBlock block
                && block.isAlive();
    }

    private int[] findAnyFreeTile(GameSession session, int maxCol) {
        for (int row = 0; row < session.getLawn().getRows(); row++) {
            for (int col = 0; col < maxCol; col++) {
                if (!hasGlacierBlock(session, row, col)) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }
}