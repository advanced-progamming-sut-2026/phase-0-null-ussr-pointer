package com.ussr.pvz.model.entities.projectiles;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

//TODO: Nuts bounce one row too early(should bounce off roll 4 but bounces off at roll 3). fix this
public class BowlingNutProjectile extends Projectile {

    public enum NutType { NORMAL, EXPLODING, GIANT }

    private final NutType nutType;
    private int deflectionCount = 0;

    private static final double NUT_SPEED = 4.0;

    public BowlingNutProjectile(Vec2 position, NutType nutType) {
        this(position, nutType, defaultVerticalSign(position));
    }

    public BowlingNutProjectile(Vec2 position, NutType nutType, double verticalSign) {
        super(null, position, diagonalVelocity(verticalSign), resolveDamage(nutType), null, null);
        this.nutType = nutType;
    }

    private static double defaultVerticalSign(Vec2 position) {
        GameSession session = App.getGameSession();
        double midRow = (session != null && session.getLawn() != null)
                ? (session.getLawn().getRows() - 1) / 2.0
                : 2.0;
        return position.y() < midRow ? 1.0 : -1.0;
    }

    private static Vec2 diagonalVelocity(double verticalSign) {
        double rad = Math.toRadians(45);
        double sign = verticalSign >= 0 ? 1.0 : -1.0;
        return Vec2.of(NUT_SPEED * Math.cos(rad), NUT_SPEED * Math.sin(rad) * sign);
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

        Vec2 prevPos = getPosition();
        Vec2 step = getSpeed().scale(GameClock.SECONDS_PER_TICK);
        setPosition(prevPos.add(step));

        // Fix: Proper Wall Bounce Logic
        int maxRow = session.getLawn().getRows() - 1;
        if (getPosition().y() <= 0 || getPosition().y() >= maxRow) {
            double clampedY = Math.max(0, Math.min(maxRow, getPosition().y()));
            setPosition(Vec2.of(getPosition().x(), clampedY));
            // Simply invert the Y velocity to bounce off the wall
            setSpeed(Vec2.of(getSpeed().x(), -getSpeed().y()));
        }

        // Despawn if it rolls off the right edge
        if (getPosition().x() >= session.getLawn().getCols()) {
            setAlive(false);
            return;
        }

        checkZombieCollision(session, prevPos);
    }

    private void checkZombieCollision(GameSession session, Vec2 prevPos) {
        Vec2 currentPos = getPosition();

        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) continue;

            Vec2 zombiePos = zombie.getPosition();
            double distance = zombiePos.distanceTo(currentPos);

            boolean withinRadius = distance <= 0.8;
            boolean sameLane = Math.abs(zombiePos.y() - currentPos.y()) < 0.5;
            boolean crossedX = sameLane && getSpeed().x() > 0
                    && prevPos.x() <= zombiePos.x() && currentPos.x() >= zombiePos.x();

            if (withinRadius || crossedX) {
                switch (nutType) {
                    case NORMAL -> {
                        zombie.takeDamage(getDamage(), this);
                        deflect(session);
                    }
                    case EXPLODING -> {
                        explode(session);
                        setAlive(false);
                    }
                    case GIANT -> {
                        zombie.takeDamage(getDamage(), this);
                        // Keeps rolling straight, piercing zombies
                    }
                }
                break; // Prevent multi-hit on stacked zombies in a single tick
            }
        }
    }

    private void deflect(GameSession session) {
        deflectionCount++;
        double speedMag = getSpeed().length();

        if (deflectionCount == 1) {
            // First hit: 45 degree deflection towards the center or randomly
            double midRow = (session.getLawn().getRows() - 1) / 2.0;
            double sign = (getPosition().y() < midRow) ? 1.0 : -1.0;
            double rad = Math.toRadians(45);
            // Calculate new velocity vector maintaining forward momentum
            setSpeed(Vec2.of(speedMag * Math.cos(rad), speedMag * Math.sin(rad) * sign));
        } else {
            // Subsequent hits: 90 degree deflection relative to current path (ricochet effect)
            // By negating the Y velocity, it accurately simulates a 90-degree zig-zag bounce
            setSpeed(Vec2.of(getSpeed().x(), -getSpeed().y()));
        }
    }

    private void explode(GameSession session) {
        double currentX = getPosition().x();
        double currentY = getPosition().y();

        for (Zombie z : session.getZombies()) {
            if (z.isAlive() && Math.abs(z.getPosition().x() - currentX) <= 1.5 &&
                    Math.abs(z.getPosition().y() - currentY) <= 1.5) {
                z.takeDamage(getDamage(), this);
            }
        }
    }
}