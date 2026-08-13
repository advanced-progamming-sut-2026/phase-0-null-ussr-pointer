package com.ussr.pvz.model.entities.zombies.zomboss.moves;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFreezer;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossMove;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IceWindMove implements ZombossMove {
    private static final Pattern WIND_CLIP_PATTERN = Pattern.compile("wind_(\\d+)");
    private static final int FREEZE_STACKS = 1;

    @Override
    public void execute(ZombossController controller, GameSession session, List<String> playingClips) {
        int numRows = session.getLawn().getRows();
        Integer x = extractWindIndex(playingClips);

        int r1;
        int r2;
        if (x != null && x >= 0 && x + 1 < numRows) {
            r1 = x;
            r2 = x + 1;
        } else {
            r1 = -1;
            r2 = -1;
        }

        if (r1 < 0) {
            Random random = new Random();
            r1 = random.nextInt(numRows);
            r2 = random.nextInt(numRows);
            while (r2 == r1 && numRows > 1) {
                r2 = random.nextInt(numRows);
            }
        }

        freezeRow(session, r1);
        freezeRow(session, r2);
    }

    @Override
    public void execute(ZombossController controller, GameSession session) {
        execute(controller, session, List.of());
    }

    private void freezeRow(GameSession session, int row) {
        if (row < 0 || row >= session.getLawn().getRows()) return;

        for (Plant plant : session.getPlants()) {
            if (plant.getLocation() != null && plant.getLocation().y() == row) {
                PlantFreezer.applyFreeze(session, plant, FREEZE_STACKS);
            }
        }
    }

    private Integer extractWindIndex(List<String> clips) {
        if (clips == null) return null;
        for (String clip : clips) {
            if (clip == null) continue;
            Matcher m = WIND_CLIP_PATTERN.matcher(clip);
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