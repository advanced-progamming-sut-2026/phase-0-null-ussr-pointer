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

    public static GrantArmor forWallNut() {
        return new GrantArmor(4000, 0, false, false, false, true);
    }

    public static GrantArmor forTallNut() {
        return new GrantArmor(8000, 0, false, false, false, true);
    }

    public static GrantArmor forEndurian() {
        return new GrantArmor(3000, 50, false, false, false, true);
    }

    public static GrantArmor forGarlic() {
        return new GrantArmor(0, 0, false, true, false, true);
    }

    public static GrantArmor forSweetPotato() {
        return new GrantArmor(0, 0, false, false, true, true);
    }

    public static GrantArmor forExplodeONut() {
        System.out.println("explode");
        return new GrantArmor(4000, 0, true, false, false, true);
    }

    public static GrantArmor forPumpkin() {
        return new GrantArmor(4000, 0, false, false, false, true);
    }

    public static GrantArmor forSunBean() {
        return new GrantArmor(1000, 0, false, false, false, true);
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        if (user == null || session == null) return;

        if (this.healToFull && user.getMaxHp() > 0) {
            user.setHp(user.getMaxHp());
        }

        applyStatusModifiers(user);

        int currentLane = (int) user.getPosition().y();

        // 3. Garlic effect: Disperse zombies in the same lane to upper/lower lanes
        if (this.disperseZombies && session.getZombies() != null) {
            for (Zombie zombie : session.getZombies()) {
                if (zombie != null && zombie.isAlive() && (int) zombie.getPosition().y() - currentLane < 0.5) {
                    int alternateLane = currentLane + (Math.random() > 0.5 ? 1 : -1);
                    if (alternateLane < 1) alternateLane = 2;
                    if (alternateLane > 5) alternateLane = 4;
                    zombie.setPosition(new Vec2(zombie.getPosition().x(), alternateLane));
                }
            }
        }

        if (this.attractZombies && session.getZombies() != null) {
            for (Zombie zombie : session.getZombies()) {
                if (zombie != null && zombie.isAlive()) {
                    int zombieLane = (int) zombie.getPosition().y();
                    if (Math.abs(zombieLane - currentLane) <= 1 && zombieLane != currentLane) {
                        zombie.setPosition(new Vec2(zombie.getPosition().x(), currentLane));
                    }
                }
            }
        }
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        if (this.armorAmount > 0 && user != null) {
            user.setArmor(new PlantArmor(this.armorAmount, this.reflectiveDamage, this.explodeOnBreak));
        }
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {
        // Instant defensive superpowers do not require ongoing tick logic
    }
}