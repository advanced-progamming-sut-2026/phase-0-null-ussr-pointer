package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.PlantFreezer;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColumnFreezeMove implements ZombossMove {
    private static final Pattern GLACIER_CLIP_PATTERN = Pattern.compile("glacier_column_(\\d+)");
    private static final int GLACIER_COLUMN_BASE = 7;
    private static final int FREEZE_STACKS = 3;

    @Override
    public void execute(ZombossController controller, GameSession session, List<String> playingClips) {
        int numCols = session.getLawn().getCols();
        Integer x = extractGlacierIndex(playingClips);

        int col = -1;
        if (x != null) {
            int candidate = GLACIER_COLUMN_BASE - x;
            if (candidate >= 0 && candidate < numCols) {
                col = candidate;
            }
        }

        if (col < 0) {
            col = new Random().nextInt(numCols);
        }

        freezeColumn(session, col);
    }

    @Override
    public void execute(ZombossController controller, GameSession session) {
        execute(controller, session, List.of());
    }

    private void freezeColumn(GameSession session, int col) {
        for (int row = 0; row < session.getLawn().getRows(); row++) {
            Cell cell = session.getLawn().getCell(row, col);
            if (cell != null && cell.getPlant() != null) {
                PlantFreezer.applyFreeze(session, cell.getPlant(), FREEZE_STACKS);
            }
            if (session.getLawn().getTile(row, col) != null) {
                session.getLawn().getTile(row, col).setType(TileType.Frozen);
            }
            try {
                Zombie frozenZombie = ZombieFactory.create("ZombieArmor1", row, col);
                session.spawnZombie(frozenZombie);
            } catch (Exception ignored) {
            }
        }
    }

    private Integer extractGlacierIndex(List<String> clips) {
        if (clips == null) return null;
        for (String clip : clips) {
            if (clip == null) continue;
            Matcher m = GLACIER_CLIP_PATTERN.matcher(clip);
            if (m.find()) {
                try {
                    return Integer.parseInt(m.group(1));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}