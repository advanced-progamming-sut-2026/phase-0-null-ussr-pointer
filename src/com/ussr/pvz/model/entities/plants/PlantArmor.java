package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.entities.zombies.Zombie;

public class PlantArmor {
    private int hp;
    private final int reflectiveDamage;
    private final boolean explodeOnBreak;

    public PlantArmor(int hp, int reflectiveDamage, boolean explodeOnBreak) {
        this.hp = hp;
        this.reflectiveDamage = reflectiveDamage;
        this.explodeOnBreak = explodeOnBreak;
    }

    /**
     * Deducts damage from the shield.
     * @return The remaining overflow damage that should pass through to the plant's base health.
     */
    public int absorbDamage(int damage, Plant user) {
        if (damage >= this.hp) {
            int overflow = damage - this.hp;
            this.hp = 0;

            if (this.explodeOnBreak) {
                executeArmorExplosion(user);
            }
            return overflow;
        } else {
            this.hp -= damage;
            return 0; // Shield absorbed everything!
        }
    }

    public void handleReflection(Zombie dealer, Plant user) {
        if (dealer != null && this.reflectiveDamage > 0 && this.hp > 0) {
            dealer.takeDamage(this.reflectiveDamage, user);
        }
    }

    public boolean isDestroyed() {
        return this.hp <= 0;
    }

    private void executeArmorExplosion(Plant user) {
        com.ussr.pvz.model.engine.GameSession session = com.ussr.pvz.model.App.getGameSession();
        if (session == null || session.getZombies() == null) return;

        System.out.println("boooom!!!!");

        com.ussr.pvz.model.util.Vec2 plantPos = user.getPosition();
        int explosionDamage = 500; // Standard baseline explosion damage

        for (com.ussr.pvz.model.entities.zombies.Zombie zombie : session.getZombies()) {
            if (zombie.isAlive()) {
                double distance = zombie.getPosition().distanceTo(plantPos);
                if (distance <= 1.5) { // 1.5 tile radius
                    zombie.takeDamage(explosionDamage);
                }
            }
        }
    }
}