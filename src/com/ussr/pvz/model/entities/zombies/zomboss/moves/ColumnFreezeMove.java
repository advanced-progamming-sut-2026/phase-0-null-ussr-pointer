package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColumnFreezeMove implements ZombossMove {
    private static final Pattern GLACIER_CLIP_PATTERN = Pattern.compile("glacier_column_(\\d+)");
    private static final int GLACIER_COLUMN_BASE = 6;
    private static final int[] FULL_SWEEP_ORDER = {1, 2, 3, 4, 5, 6};

    @Override
    public void execute(ZombossController controller, GameSession session, List<String> playingClips) {
        List<Integer> indices = extractGlacierIndices(playingClips);
        if (indices.isEmpty()) {
            for (int x : FULL_SWEEP_ORDER) {
                indices.add(x);
            }
        }

        int numCols = session.getLawn().getCols();
        List<Integer> columns = new ArrayList<>();
        for (int x : indices) {
            int col = GLACIER_COLUMN_BASE - x;
            if (col >= 0 && col < numCols) {
                columns.add(col);
            }
        }
        if (columns.isEmpty()) return;

        GlacierColumnSweep sweep = new GlacierColumnSweep(session, columns);
        session.registerTickable(sweep);
    }

    @Override
    public void execute(ZombossController controller, GameSession session) {
        execute(controller, session, List.of());
    }

    private List<Integer> extractGlacierIndices(List<String> clips) {
        List<Integer> indices = new ArrayList<>();
        if (clips == null) return indices;
        for (String clip : clips) {
            if (clip == null) continue;
            Matcher m = GLACIER_CLIP_PATTERN.matcher(clip);
            if (m.find()) {
                try {
                    indices.add(Integer.parseInt(m.group(1)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return indices;
    }
}