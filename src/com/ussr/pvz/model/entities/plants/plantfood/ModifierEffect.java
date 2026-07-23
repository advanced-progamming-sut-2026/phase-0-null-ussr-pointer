package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.actstrategy.ModifyStrategy;

public class ModifierEffect implements PlantFoodEffect{
    private final boolean isCloner;
    private final int damageMultiplier;

    public ModifierEffect(boolean isCloner, int damageMultiplier) {
        this.isCloner = isCloner;
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (this.isCloner) {
            for (int row = 0; row < session.getLawn().getRows(); row++) {
                for (int col = 0; col < session.getLawn().getCols(); col++) {
                    Cell cell = session.getLawn().getCell(row, col);
                    if (cell != null && cell.getTile() != null && cell.getTile().getType() == TileType.Water) {

                        boolean isTileOccupied = false;
                        if (session.getPlants() != null) {
                            for (Plant plant : session.getPlants()) {
                                if (plant.isAlive() && (int) plant.getPosition().x() == col &&
                                        (int) plant.getPosition().y() == row) {
                                    isTileOccupied = true;
                                    break;
                                }
                            }
                        }

                        if (!isTileOccupied) {
                            Plant lilyPadClone = new Plant(user);
                            session.getPlants().add(lilyPadClone);
                        }
                    }
                }
            }
        }

    }
    @Override
    public void applyStatusModifiers(Plant user) {
        if(damageMultiplier != 1) {
            if(user.getActStrategy() instanceof ModifyStrategy)
                ((ModifyStrategy) user.getActStrategy()).setDamageMultiplier(damageMultiplier);
        }
        user.setBuffed(true);
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {

    }
}
