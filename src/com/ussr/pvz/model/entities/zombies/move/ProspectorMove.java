package com.ussr.pvz.model.entities.zombies.move;

import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class ProspectorMove implements MoveBehavior {
    private enum ProspectorPhase {
        WALKING_LEFT,
        AIRBORNE_LAUNCH,
        REVERSE_WALK
    }

    private ProspectorPhase currentPhase = ProspectorPhase.WALKING_LEFT;

    private double launchCountdown = 10.0;
    private double timeToTravel = 1.5;
    private double airborneSpeedX = 0.0;

    private boolean dynamiteExtinguished = false;

    public void extinguishDynamite() {
        if (currentPhase == ProspectorPhase.WALKING_LEFT) {
            dynamiteExtinguished = true;
        }
    }

    public void litDynamite() {
        if (currentPhase == ProspectorPhase.WALKING_LEFT) {
            if (dynamiteExtinguished) {
                dynamiteExtinguished = false;
            }
        }
    }

    @Override
    public void move(
            Zombie zombie,
            GameSession session,
            float delta
    ) {
        if (dynamiteExtinguished) {
            new NormalWalk().move(zombie, session , delta);
            return;
        }

        Vec2 pos = zombie.getPosition();
        if (pos == null) return;

        switch (currentPhase) {
            case WALKING_LEFT -> {
                Vec2 vel = zombie.getSpeed();
                Vec2 newPos = pos.add(vel.scale(delta));
                zombie.setPosition(newPos);

                launchCountdown -= delta;
                if (launchCountdown <= 0) {
                    currentPhase = ProspectorPhase.AIRBORNE_LAUNCH;

                    if (pos.x() > 0) {
                        airborneSpeedX = pos.x() / timeToTravel;
                    }
                }
            }

            case AIRBORNE_LAUNCH -> {
                double newX = pos.x() - (airborneSpeedX * delta);
                zombie.setPosition(Vec2.of(Math.max(0, newX), pos.y()));

                timeToTravel -= delta;
                if (timeToTravel <= 0 || zombie.getPosition().x() <= 0) {
                    zombie.setPosition(Vec2.of(0, pos.y()));
                    currentPhase = ProspectorPhase.REVERSE_WALK;

                    double standardSpeedMagnitude = Math.abs(zombie.getSpeed().x());
                    zombie.setSpeed(Vec2.of(standardSpeedMagnitude, 0));
                }
            }

            case REVERSE_WALK -> {
                Vec2 vel = zombie.getSpeed();
                Vec2 newPos = pos.add(vel.scale(delta));
                zombie.setPosition(newPos);
            }
        }
    }
}