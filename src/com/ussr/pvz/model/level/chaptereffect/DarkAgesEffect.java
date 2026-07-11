package com.ussr.pvz.model.level.chaptereffect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.Lawn;
import com.ussr.pvz.model.board.structures.Grave;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

import java.util.Random;

/**
 * At the start of each wave, new graves may appear on empty Grave /
 * Necromancy tiles (skipping any tile with a plant on it already). Some
 * graves hide sun or a plant food. Necromancy tiles additionally have a
 * chance to raise a zombie from beneath the grave immediately.
 */
public class DarkAgesEffect implements ChapterEffect {

    private static final Random RAND = new Random();
    private static final double NEW_GRAVE_CHANCE = 0.35;
    private static final double NECROMANCY_RAISE_CHANCE = 0.5;
    private static final String RISEN_ZOMBIE_ID = "ZombieDefault"; // swap for a Dark Ages alias if you have one

    @Override
    public void onWaveStart(GameSession session, Level level, int waveNumber, boolean isFinalWave) {
        Lawn lawn = session.getLawn();
        if (lawn == null) return;

        for (int r = 0; r < lawn.getRows(); r++) {
            for (int c = 0; c < lawn.getCols(); c++) {
                Cell cell = lawn.getCell(r, c);
                if (cell == null || cell.getTile() == null) continue;

                TileType type = cell.getTile().getType();
                if (type != TileType.Grave && type != TileType.Necromancy) continue;
                if (cell.getPlant() != null) continue;
                if (cell.getInteractableStructure() instanceof Grave) continue;
                if (RAND.nextDouble() >= NEW_GRAVE_CHANCE) continue;

                Grave.Content content = rollContent();
                Grave grave = new Grave(null, content);
                grave.setPosition(new Vec2(c, r));
                cell.setStructure(grave);
                session.registerStructure(grave);

                System.out.println("A tombstone rises from the ground at (" + c + ", " + r + ")!");

                if (type == TileType.Necromancy && RAND.nextDouble() < NECROMANCY_RAISE_CHANCE) {
                    raiseZombie(session, r, c);
                }
            }
        }
    }

    private Grave.Content rollContent() {
        double roll = RAND.nextDouble();
        if (roll < 0.15) return Grave.Content.SUN;
        if (roll < 0.25) return Grave.Content.PLANT_FOOD;
        return Grave.Content.NONE;
    }

    private void raiseZombie(GameSession session, int row, int col) {
        try {
            Zombie zombie = ZombieFactory.create(RISEN_ZOMBIE_ID, row, col);
            session.spawnZombie(zombie);
            System.out.println("A zombie claws its way out of the grave at (" + col + ", " + row + ")!");
        } catch (IllegalArgumentException e) {
            System.err.println("[DarkAgesEffect] Could not raise zombie: " + e.getMessage());
        }
    }
}