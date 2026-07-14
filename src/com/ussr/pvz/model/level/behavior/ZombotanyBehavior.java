package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.projectiles.hit.NormalHit;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;
import com.ussr.pvz.model.entities.zombies.attack.KamikazeAttack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ZombotanyBehavior extends LevelBehavior {

    private final Random rand = new Random();
    private static final String[] ZOMBOTANY_TYPES = {
            "peashooter-zombie", "wall-nut-zombie", "jalapeno-zombie", "squash-zombie"
    };

    private final Map<Zombie, Double> jalapenoTimers = new HashMap<>();
    private final Map<Zombie, Double> peashooterTimers = new HashMap<>();

    public ZombotanyBehavior() {
    }

    @Override
    public void onStart(Level level) {
        super.onStart(level);
        GameSession session = App.getGameSession();
        if (session != null) {
            session.getEventBus().subscribe(GameEvent.ZombieSpawned.class, this::mutateZombie);
        }
    }

    private void mutateZombie(GameEvent.ZombieSpawned event) {
        // Base case to prevent infinite recursion when we spawn the mutants
        if (event.alias().contains("-zombie")) return;

        GameSession session = App.getGameSession();
        if (session == null) return;

        List<Zombie> zombies = session.getZombies();
        if (zombies.isEmpty()) return;

        Zombie targetToMutate = null;
        for (Zombie z : zombies) {
            if (z.isAlive() && z.getAlias().equals(event.alias()) &&
                    (int) z.getPosition().y() == event.lane() &&
                    (int) z.getPosition().x() == event.col()) {
                targetToMutate = z;
                break;
            }
        }

        if (targetToMutate == null) return;

        String zombotanyType = ZOMBOTANY_TYPES[rand.nextInt(ZOMBOTANY_TYPES.length)];

        // Flag the vanilla zombie for garbage collection by the GameClock.
        targetToMutate.setAlive(false);

        try {
            Zombie mutated = ZombieFactory.create(
                    zombotanyType,
                    (int) targetToMutate.getPosition().y(),
                    (int) targetToMutate.getPosition().x()
            );

            // Hardcode mechanical overrides since the JSON factory definitions for these specific types likely do not exist.
            switch (zombotanyType) {
                case "wall-nut-zombie" -> {
                    mutated.setMaxHp(4000);
                    mutated.setHp(4000);
                }
                case "squash-zombie" -> {
                    mutated.setSpeed(Vec2.of(-0.4, 0));
                    mutated.setAttackBehavior(new KamikazeAttack(9999));
                }
                case "jalapeno-zombie" -> jalapenoTimers.put(mutated, 0.0);
                case "peashooter-zombie" -> peashooterTimers.put(mutated, 0.0);
            }

            session.spawnZombie(mutated);
        } catch (IllegalArgumentException e) {
            // Failsafe: if the JSON is missing the Zombotany definitions entirely, revert to the normal zombie
            targetToMutate.setAlive(true);
        }
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        super.tick(session, deltaTime);
        if (levelCompleted || session.isGameOver()) return;

        jalapenoTimers.entrySet().removeIf(entry -> {
            Zombie z = entry.getKey();
            if (!z.isAlive()) return true;

            double time = entry.getValue() + deltaTime;
            if (time >= 10.0) {
                triggerJalapenoExplosion(session, z);
                z.setAlive(false);
                session.notifyZombieDied(z, "Jalapeno Self-Destruct");
                return true;
            }
            entry.setValue(time);
            return false;
        });

        peashooterTimers.entrySet().removeIf(entry -> {
            Zombie z = entry.getKey();
            if (!z.isAlive()) return true;

            double time = entry.getValue() + deltaTime;
            if (time >= 1.5) {
                firePea(session, z);
                entry.setValue(0.0);
            } else {
                entry.setValue(time);
            }
            return false;
        });
    }

    private void triggerJalapenoExplosion(GameSession session, Zombie jalapenoZombie) {
        int lane = (int) jalapenoZombie.getPosition().y();
        if (session.getPlants() != null) {
            for (Plant p : session.getPlants()) {
                if (p.isAlive() && p.getLocation() != null && p.getLocation().y() == lane) {
                    p.takeDamage(p.getHp());
                }
            }
        }
    }

    private void firePea(GameSession session, Zombie shooterZombie) {
        int lane = (int) shooterZombie.getPosition().y();
        Plant target = null;
        double closestDist = Double.MAX_VALUE;

        if (session.getPlants() != null) {
            for (Plant p : session.getPlants()) {
                if (p.isAlive() && p.getLocation() != null && p.getLocation().y() == lane) {
                    double dist = shooterZombie.getPosition().x() - p.getLocation().x();
                    if (dist > 0 && dist < closestDist) {
                        closestDist = dist;
                        target = p;
                    }
                }
            }
        }

        if (target != null) {
            // StraightMove defaults to moving Right (positive X).
            // Passing the plant as the Target ensures StraightMove calculates the delta
            // and forces the velocity vector to head Left (negative X).
            Projectile pea = new Projectile(
                    (Damageable) target,
                    Vec2.of(shooterZombie.getPosition().x() - 0.5, shooterZombie.getPosition().y()),
                    Vec2.of(-4.0, 0),
                    20,
                    new StraightMove(),
                    new NormalHit(1)
            );
            session.addProjectile(pea);
        }
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
        super.onWaveComplete(level, waveNumber);
    }

    @Override
    public void onComplete(Level level) {
        super.onComplete(level);
    }

    @Override
    public boolean isFailed(Level level) {
        return false;
    }
}