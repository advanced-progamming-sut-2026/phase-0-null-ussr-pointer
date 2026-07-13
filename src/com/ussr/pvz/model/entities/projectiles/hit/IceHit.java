package com.ussr.pvz.model.entities.projectiles.hit;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.IceBlock;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.effect.FireEffect;
import com.ussr.pvz.model.entities.zombies.move.ProspectorMove;
import java.util.ArrayList;

public class IceHit implements HitEffectStrategy {
    private static final int PLANT_FREEZE_STACKS = 3;
    private static final int ICE_BLOCK_HP = 500;

    private int areaLength;

    public IceHit(int areaLength) {
        this.areaLength = areaLength;
    }

    @Override
    public void apply(ArrayList<GameEntity> entities, Projectile projectile) {
        if (entities == null || projectile == null) {
            return;
        }

        projectile.setAlive(false);

        int damageAmount = projectile.getDamage();
        long projectileLane = Math.round(projectile.getPosition().y());

        for (GameEntity target : entities) {
            if (target == null || !target.isAlive()) continue;

            switch (target) {
                case Zombie zombie -> {
                    zombie.takeDamage(damageAmount,projectile);

                    if (zombie.getEffectStatus() instanceof FireEffect fireEffect) {
                        fireEffect.setLit(false);
                    }

                    if (zombie.getMoveBehavior() instanceof ProspectorMove prospectorMove) {
                        prospectorMove.extinguishDynamite();
                    }

                    zombie.setStatus(Zombie.Status.FREEZE);
                }
                case Plant plant -> {
                    plant.takeDamage(damageAmount);
                    applyChill(plant);
                }
                case InteractableStructure structure -> structure.takeDamage(damageAmount);
                default -> {
                }
            }
        }
    }

    private void applyChill(Plant plant) {
        if (!plant.isAlive() || plant.getState() == Plant.PlantState.INCAPACITATED) return;

        int newLevel = plant.getChillLevel() + 1;
        plant.setChillLevel(newLevel);

        if (newLevel >= PLANT_FREEZE_STACKS) {
            GameSession session = App.getGameSession();
            if (session == null || session.getLawn() == null || plant.getLocation() == null) return;

            Cell cell = session.getLawn().getCell(plant.getLocation().y(), plant.getLocation().x());
            if (cell == null) return;

            IceBlock iceBlock = new IceBlock(plant, ICE_BLOCK_HP);
            cell.setStructure(iceBlock);
            session.registerStructure(iceBlock);
        }
    }

    @Override
    public int getAreaLength() {
        return areaLength;
    }
}