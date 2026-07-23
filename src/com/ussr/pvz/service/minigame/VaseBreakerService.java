package com.ussr.pvz.service.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.SeedPackDrop;
import com.ussr.pvz.model.entities.plants.Plant;

public class VaseBreakerService {

    public String smashVase(int x, int y) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return "Game session not active.";

        Cell cell = session.getLawn().getCell(y, x);
        if (cell == null || cell.getInteractableStructure() == null) {
            return "No vase found at location (" + x + ", " + y + ").";
        }

        if (cell.getInteractableStructure() instanceof Vase vase) {
            if (vase.isAlive()) {
                // Instantly break the vase
                vase.takeDamage(vase.getHp() + 20);
                return "Vase smashed at (" + x + ", " + y + ")!";
            }
        }
        return "No active vase at that location.";
    }

    public String plantFromSeedPack(int sX, int sY, int x, int y) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return "Game session not active.";

        Cell cell = session.getLawn().getCell(y, x);
        if (cell == null) return "Invalid planting location.";
        if (cell.getPlant() != null) return "Tile is already occupied by a plant.";

        SeedPackDrop targetPack = null;

        for (GroundItem item : session.getItems()) {
            if (item.getItemType() == ItemType.SEED_PACK && item.getLocation().x() == sX
                    && item.getLocation().y() == sY) {
                targetPack = (SeedPackDrop) item;
                break;
            }
        }

        if (targetPack == null) return "No seed pack found at that location.";

        try {
            Plant newPlant = com.ussr.pvz.model.entities.plants.PlantFactory.createPlant(targetPack.getPlantId(), 1);
            newPlant.setLocation(new Plant.Location(x, y));
            newPlant.setPosition(com.ussr.pvz.model.util.Vec2.of(x, y));

            cell.setPlant(newPlant);
            session.addPlant(newPlant);

            targetPack.setAlive(false); // consume the seedpack

            return "Successfully planted " + newPlant.getName() + " at (" + x + ", " + y + ")!";
        } catch (Exception e) {
            return "Failed to plant from seed pack: " + e.getMessage();
        }
    }
}