package com.ussr.pvz.model.entities.projectiles.hit;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFreezer;
import com.ussr.pvz.model.entities.plants.upgrades.SpecialUpgrade;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.effect.FireEffect;
import com.ussr.pvz.model.entities.zombies.move.ProspectorMove;
import java.util.ArrayList;

public class IceHit implements HitEffectStrategy {
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

                    double bonusDuration = 0.0;
                    Plant source = projectile.getUser();
                    if (source != null) {
                        bonusDuration += source.getSpecialUpgradeValue(
                                SpecialUpgrade.CHILL_DURATION_EXT);
                        bonusDuration += source.getSpecialUpgradeValue(
                                SpecialUpgrade.FREEZE_DURATION_EXT);
                    }
                    zombie.setStatus(
                            Zombie.Status.FREEZE,
                            Zombie.DEFAULT_FREEZE_DURATION + bonusDuration
                    );
                }
                case Plant plant -> {
                    plant.takeDamage(damageAmount);
                    PlantFreezer.applyFreeze(App.getGameSession(), plant, 1);
                }
                case InteractableStructure structure -> structure.takeDamage(damageAmount);
                default -> {
                }
            }
        }
    }

    @Override
    public int getAreaLength() {
        return areaLength;
    }
}
