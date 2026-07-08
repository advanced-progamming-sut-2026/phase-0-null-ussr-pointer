package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class BowlingNutProjectile extends Projectile {

    public enum NutType { NORMAL, EXPLODING, GIANT }

    private final NutType nutType;
    private int bounceDirection = 0; // 0 = straight, 1 = up (negative Y), -1 = down (positive Y)

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

        // Move the nut based on current direction
        Vec2 pos = getPosition();
        double speedX = getSpeed().x() * GameClock.SECONDS_PER_TICK;
        double speedY = (bounceDirection * 15.0) * GameClock.SECONDS_PER_TICK;

        setPosition(pos.add(Vec2.of(speedX, speedY)));

        // Handle wall bounces (top and bottom of lawn)
        if (getPosition().y() <= 0) bounceDirection = 1; // Bounce down
        if (getPosition().y() >= session.getLawn().getRows() - 1) bounceDirection = -1; // Bounce up

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
                        // Bounce at a 45-degree angle
                        bounceDirection = (bounceDirection == 0) ? (Math.random() > 0.5 ? 1 : -1) : -bounceDirection;
                    }
                    case EXPLODING -> {
                        explode(session);
                        setAlive(false); // Destroy nut on explosion
                    }
                    case GIANT -> {
                        zombie.takeDamage(getDamage(), this);
                        // Keeps rolling straight, piercing zombies
                    }
                }
                break; // Only trigger hit on one zombie per tick for Normal/Exploding
            }
        }
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