package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.board.Cell;

import java.util.ArrayList;
import java.util.List;

public class WizardEffect implements EffectStatus {
    private final double transformInterval;
    private double timer = 0.0;

    private final List<Damageable> cursedEntities = new ArrayList<>();
    private boolean deathHandled = false;

    public WizardEffect(double transformInterval) {
        this.transformInterval = transformInterval;
    }

    @Override
    public void effect(Zombie wizard, GameSession session) {
        if (!wizard.isAlive()) {
            if (!deathHandled) {
                for (Damageable cursed : cursedEntities) {
                    if (cursed.isAlive()) {
                        if (cursed instanceof Plant p) {
                            p.setState(Plant.PlantState.ACTIVE);
                        } else if (cursed instanceof Zombie z) {
                            z.setStatus(Zombie.Status.NORMAL);
                        }
                    }
                }
                cursedEntities.clear();
                deathHandled = true;
            }
            return;
        }

        timer += GameClock.SECONDS_PER_TICK;
        if (timer >= transformInterval) {
            timer = 0;
            transformRandomTarget(wizard, session);
        }

        handleCollisionTransformation(wizard, session);
    }

    private void transformRandomTarget(Zombie wizard, GameSession session) {
        List<Damageable> validTargets = new ArrayList<>();

        if (wizard.getFaction() == Faction.ZOMBIES) {
            for (Plant p : session.getPlants()) {
                if (p.isAlive() && !p.getState().equals(Plant.PlantState.INCAPACITATED)) {
                    validTargets.add(p);
                }
            }
        } else {
            for (Zombie z : session.getZombies()) {
                if (z.isAlive() && z != wizard && z.getStatus() != Zombie.Status.FREEZE) {
                    validTargets.add(z);
                }
            }
        }

        if (!validTargets.isEmpty()) {
            int randomIndex = (int) (Math.random() * validTargets.size());
            applyCurse(validTargets.get(randomIndex));
        }
    }

    private void handleCollisionTransformation(Zombie wizard, GameSession session) {
        Damageable target = wizard.acquireTarget(session);
        if (target != null && target.isAlive()) {
            applyCurse(target);
        }
    }

    private void applyCurse(Damageable target) {
        if (target instanceof Plant p) {
            p.setState(Plant.PlantState.INCAPACITATED);
        } else if (target instanceof Zombie z) {
            z.setStatus(Zombie.Status.FREEZE);
        }
        cursedEntities.add(target);
    }
}