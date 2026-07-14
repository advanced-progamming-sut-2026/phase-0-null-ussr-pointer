package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class BowlingNutProjectile extends Projectile {

    public enum NutType { NORMAL, EXPLODING, GIANT }

    private final NutType nutType;
    private int deflectionCount = 0;

    public BowlingNutProjectile(Vec2 position, NutType nutType) {
        super(null, position, Vec2.of(15.0, 0), resolveDamage(nutType), null, null);
        this.nutType = nutType;
    }

    private static int resolveDamage(NutType type) {
        return switch (type) {
            case NORMAL -> 600;
            case EXPLODING -> 1800; // Instant kill in 3x3
            case GIANT -> 4000;     // Crushes everything
        };
    }

    @Override
    public void tick() {
        if (!isAlive()) return;
        GameSession session = App.getGameSession();
        if (session == null) return;

        Vec2 pos = getPosition();
        Vec2 step = getSpeed().scale(GameClock.SECONDS_PER_TICK);
        setPosition(pos.add(step));

        // Handle wall bounces (top and bottom of lawn)
        int maxRow = session.getLawn().getRows() - 1;
        if (getPosition().y() <= 0 || getPosition().y() >= maxRow) {
            // Clamp back inside the lawn so it doesn't keep drifting out before the turn takes effect
            double clampedY = Math.max(0, Math.min(maxRow, getPosition().y()));
            setPosition(Vec2.of(getPosition().x(), clampedY));
            deflect(session);
        }

        // Despawn if it rolls off the right edge
        if (getPosition().x() >= session.getLawn().getCols()) {
            setAlive(false);
            return;
        }

        // Check collision with zombies
        checkZombieCollision(session);
    }

    private void checkZombieCollision(GameSession session) {
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) continue;

            double distance = zombie.getPosition().distanceTo(getPosition());
            if (distance <= 0.8) {

                switch (nutType) {
                    case NORMAL -> {
                        zombie.takeDamage(getDamage(), this);
                        deflect(session);
                    }
                    case EXPLODING -> {
                        explode(session);
                        setAlive(false); // Destroy nut on explosion
                    }
                    case GIANT -> {
                        zombie.takeDamage(getDamage(), this);
                        // Keeps rolling straight, piercing zombies - no deflection
                    }
                }
                break; // Only trigger hit on one zombie per tick for Normal/Exploding
            }
        }
    }

    private void deflect(GameSession session) {
        deflectionCount++;
        double turnAmountDeg = (deflectionCount == 1) ? 45.0 : 90.0;

        double midRow = (session.getLawn().getRows() - 1) / 2.0;
        double sign = (getPosition().y() < midRow) ? 1.0 : -1.0; // turn toward the middle row

        setSpeed(rotate(getSpeed(), sign * turnAmountDeg));
    }

    private static Vec2 rotate(Vec2 v, double degrees) {
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        return Vec2.of(
                v.x() * cos - v.y() * sin,
                v.x() * sin + v.y() * cos
        );
    }

    private void explode(GameSession session) {
        double currentX = getPosition().x();
        double currentY = getPosition().y();

        // 3x3 Explosion logic
        for (Zombie z : session.getZombies()) {
            if (z.isAlive() && Math.abs(z.getPosition().x() - currentX) <= 1.5 && Math.abs(z.getPosition().y() - currentY) <= 1.5) {
                z.takeDamage(getDamage(), this);
            }
        }
    }
}