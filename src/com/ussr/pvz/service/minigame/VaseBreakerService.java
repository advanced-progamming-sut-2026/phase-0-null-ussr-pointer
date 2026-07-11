package com.ussr.pvz.service.minigame;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Vase;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.items.SeedPackDrop;

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
                vase.takeDamage(vase.getHp());
                return "Vase smashed at (" + x + ", " + y + ")!";
            }
        }
        return "No active vase at that location.";
    }

    public String plantFromSeedPack(int sX, int sY, int x, int y) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLawn() == null) return "Game session not active.";

        Cell cell = session.getLawn().getCell(x, y);
        if (cell == null) return "Invalid planting location.";
        if (cell.getPlant() != null) return "Tile is already occupied by a plant.";
        StringBuilder plantType = new StringBuilder();
        session.getItems().stream().filter(i -> i.getItemType().equals(ItemType.SEED_PACK))
                .filter(i -> i.getLocation().x() == sX && i.getLocation().y() == sY)
                .findFirst().ifPresent(i -> {
                    if (i instanceof SeedPackDrop) {
                        plantType.append(((SeedPackDrop) i).getType());
                    }
                });
        // TODO: Generate plant using the factory once it is fully available.
        // Plant newPlant = PlantFactory.createPlant(seedName, 1);
        // newPlant.setLocation(new Plant.Location(x, y));
        // cell.setPlant(newPlant);
        // session.getPlants().add(newPlant);

        return "Successfully planted " + plantType + " at (" + x + ", " + y + ")! (TODO: Factory Implementation Pending)";
    }
}