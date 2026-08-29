package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;

public class ShockwaveStrategy implements ActStrategy {

    private static final double TOLERANCE = 0.2;

    @Override
    public void act(Plant user, GameSession session) {
        if (hasHpDrivenGrowth(user) && updateStage(user)) {
            user.triggerGrowAnimation();
            return;
        }

        int half = radiusFor(user);
        double boundary = half + TOLERANCE;
        Vec2 userPos = user.getPosition();

        ArrayList<Zombie> targets = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !zombie.isAlive()) continue;
            Vec2 zombiePos = zombie.getPosition();
            if (zombiePos == null) continue;
            if (Math.abs(zombiePos.x() - userPos.x()) <= boundary
                    && Math.abs(zombiePos.y() - userPos.y()) <= boundary) {
                targets.add(zombie);
            }
        }

        if (targets.isEmpty()) return;

        int damage = user.getDamage();
        for (Zombie zombie : targets) {
            zombie.takeDamage(damage, user);
        }
        user.triggerActionAnimation();
    }

    private boolean hasHpDrivenGrowth(Plant user) {
        return "Kiwibeast".equalsIgnoreCase(user.getName());
    }

    private int radiusFor(Plant user) {
        if (hasHpDrivenGrowth(user)) {
            return user.getCurrentStage() - 1;
        }
        return 1;
    }

    private boolean updateStage(Plant user) {
        int maxHp = user.getMaxHp();
        double ratio = maxHp > 0 ? (double) user.getHp() / maxHp : 1.0;

        int stage;
        if (ratio <= 1.0 / 3.0) {
            stage = 3;
        } else if (ratio <= 2.0 / 3.0) {
            stage = 2;
        } else {
            stage = 1;
        }

        if (stage == user.getCurrentStage()) {
            return false;
        }
        user.setGrowthStage(stage);
        return true;
    }
}