package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.board.structures.IceBlock;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.util.Vec2;

/**
 * Shared freeze mechanic, mirroring FrostbiteCavesEffect.applyFreezingWind():
 * bump chillLevel, and once it hits 3 stacks wrap the plant in an IceBlock.
 * Any zomboss move that freezes plants should call this instead of a
 * fabricated Plant.freeze() (no such method exists on Plant).
 */
public final class PlantFreezer {
    public static final int ICE_BLOCK_HP = 600;

    private PlantFreezer() {
    }

    public static void applyFreeze(GameSession session, Plant plant, int stacks) {
        if (session == null || session.getLawn() == null || plant == null
                || plant.getLocation() == null || stacks <= 0 || !plant.isAlive()
                || plant.getState() == Plant.PlantState.INCAPACITATED
                || plant.getTags().contains(Tag.FIRE)) {
            return;
        }

        int oldLevel = plant.getChillLevel();
        int newLevel = Math.min(Plant.MAX_CHILL_LEVEL, oldLevel + stacks);
        plant.setChillLevel(newLevel);

        if (oldLevel < Plant.MAX_CHILL_LEVEL && newLevel == Plant.MAX_CHILL_LEVEL) {
            fullyFreeze(session, plant);
        }
    }

    public static void thawOneLevel(GameSession session, Plant plant, int iceDamage) {
        if (session == null || session.getLawn() == null || plant == null
                || plant.getLocation() == null || !plant.isAlive()
                || plant.getChillLevel() <= 0) {
            return;
        }

        int col = plant.getLocation().x();
        int row = plant.getLocation().y();

        if (plant.getChillLevel() == Plant.MAX_CHILL_LEVEL) {
            var structure = session.getLawn().getCell(row, col).getInteractableStructure();
            if (structure instanceof IceBlock iceBlock) {
                iceBlock.takeDamage(Math.max(0, iceDamage));
            }
            return;
        }

        plant.setChillLevel(plant.getChillLevel() - 1);
    }

    private static void fullyFreeze(GameSession session, Plant plant) {
        int col = plant.getLocation().x();
        int row = plant.getLocation().y();
        var cell = session.getLawn().getCell(row, col);
        if (cell == null || cell.getInteractableStructure() instanceof IceBlock) {
            return;
        }

        IceBlock iceBlock = new IceBlock(plant, ICE_BLOCK_HP);
        iceBlock.setPosition(Vec2.of(col, row));
        plant.setState(Plant.PlantState.INCAPACITATED);
        cell.setPlant(null);
        cell.setStructure(iceBlock);
        session.registerStructure(iceBlock);
    }
}
