package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.projectiles.SnowballProjectile;
import com.ussr.pvz.model.util.Vec2;

public class IceAgeHunterEffect implements EffectStatus {
    private final double farAttackRange;
    private final double nearAttackRange;

    private final double throwCooldown = 2.0;
    private double timer;

    public IceAgeHunterEffect(double farAttackRange, double nearAttackRange) {
        this.farAttackRange = farAttackRange;
        this.nearAttackRange = nearAttackRange;
        this.timer = this.throwCooldown;
    }

    @Override
    public void effect(Zombie zombie, GameSession session) {
        if (!zombie.isAlive()) return;

        timer += GameClock.SECONDS_PER_TICK;
        if (timer >= throwCooldown) {
            if (throwSnowball(zombie, session)) {
                timer = 0;
            }
        }
    }

    private boolean throwSnowball(Zombie zombie, GameSession session) {
        int zRow = (int) zombie.getPosition().y();
        double zCol = zombie.getPosition().x();
        int cols = session.getLawn().getCols();
        Vec2 startPos = zombie.getPosition();

        if (zombie.getFaction() == Faction.ZOMBIES) {
            Plant closestPlant = null;
            double closestDistance = Double.MAX_VALUE;

            for (int c = (int) zCol; c >= 0; c--) {
                if (c >= cols) continue;
                Cell cell = session.getLawn().getCell(zRow, c);
                if (cell != null && cell.getPlant() != null && cell.getPlant().isAlive()) {
                    closestPlant = cell.getPlant();
                    closestDistance = zCol - closestPlant.getLocation().x();
                    break;
                }
            }

            if (closestPlant != null && closestDistance >= nearAttackRange && closestDistance <= farAttackRange) {
                Vec2 targetPos = Vec2.of(closestPlant.getLocation().x(), closestPlant.getLocation().y());
                session.addZombieProjectile(new SnowballProjectile(startPos, targetPos, 0.8));
                return true;
            }
        } else {
            Zombie closestZombie = null;
            double closestDistance = Double.MAX_VALUE;

            for (Zombie target : session.getZombies()) {
                if (target.isAlive() && target.getFaction() == Faction.ZOMBIES &&
                        (int) target.getPosition().y() == zRow && target.getPosition().x() > zCol) {
                    double distance = target.getPosition().x() - zCol;
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestZombie = target;
                    }
                }
            }

            if (closestZombie != null && closestDistance >= nearAttackRange && closestDistance <= farAttackRange) {
                session.addZombieProjectile(new SnowballProjectile(startPos, closestZombie.getPosition(), 0.8));
                return true;
            }
        }
        return false;
    }
}