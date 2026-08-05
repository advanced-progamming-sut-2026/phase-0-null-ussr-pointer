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

    private PlantFreezer() {
    }

    public static void applyFreeze(GameSession session, Plant plant, int stacks) {
        if (plant == null || !plant.isAlive() || plant.getTags().contains(Tag.FIRE)) return;

        plant.setChillLevel(Math.min(3, plant.getChillLevel() + stacks));
        if (plant.getChillLevel() == 3 && plant.getState() != Plant.PlantState.INCAPACITATED) {
            IceBlock iceBlock = new IceBlock(plant, 600);
            int col = plant.getLocation().x();
            int row = plant.getLocation().y();
            iceBlock.setPosition(Vec2.of(col, row));

            session.getLawn().getCell(row, col).setStructure(iceBlock);
            session.registerStructure(iceBlock);
        }
    }
}