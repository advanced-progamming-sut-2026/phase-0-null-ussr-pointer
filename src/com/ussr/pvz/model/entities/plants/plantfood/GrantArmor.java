package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantArmor;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class GrantArmor implements PlantFoodEffect {
    private final int armorAmount;
    private final int reflectiveDamage;
    private final boolean explodeOnBreak;
    private final boolean disperseZombies;
    private final boolean attractZombies;
    private final boolean healToFull;

    public GrantArmor(int armorAmount, int reflectiveDamage, boolean explodeOnBreak,
                      boolean disperseZombies, boolean attractZombies, boolean healToFull) {
        this.armorAmount = armorAmount;
        this.reflectiveDamage = reflectiveDamage;
        this.explodeOnBreak = explodeOnBreak;
        this.disperseZombies = disperseZombies;
        this.attractZombies = attractZombies;
        this.healToFull = healToFull;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        // 1. Handle healing if specified (Great for Wall-nuts and Sweet Potato)
        if (this.healToFull && user.getMaxHp() > 0) {
            user.setHp(user.getMaxHp());
        }

        int currentLane = user.getLocation().y();
        double currentColumn = user.getLocation().x();

        if (this.disperseZombies) {
            for (Zombie zombie : session.getZombies()) {
                if (zombie.isAlive() && (int) zombie.getPosition().y() == currentLane) {
                    // Force the zombie up or down a lane randomly
                    int alternateLane = currentLane + (Math.random() > 0.5 ? 1 : -1);


                    if (alternateLane < 1) alternateLane = 2;
                    if (alternateLane > 5) alternateLane = 4;

                    zombie.setPosition(new Vec2(zombie.getPosition().x(), alternateLane));
                }
            }
        }

        if (this.attractZombies) {
            for (Zombie zombie : session.getZombies()) {
                if (zombie.isAlive() && Math.abs(zombie.getPosition().y() - currentLane) <= 1.5) {
                    zombie.setPosition(new Vec2(currentColumn, currentLane));
                }
            }
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        if (this.armorAmount > 0) {
            user.setArmor(new PlantArmor(this.armorAmount, this.reflectiveDamage, this.explodeOnBreak));
        }
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
    }
}
