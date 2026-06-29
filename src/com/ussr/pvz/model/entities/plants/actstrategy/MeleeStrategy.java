package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;

public class MeleeStrategy implements ActStrategy {
    @Override
    //todo:add ability value 1for front and back hit 2 for area damage 3 for wave 4 for swallow
    //todo: set a massive damage for swallow plant so that it kills the zombie with very first hit(swallow)
    public void act(Plant user, GameSession session) {
        if(user.getIntervalTimer() > 0) return;
        if(!isEnemyAround(user , session)) return;
        ArrayList<Zombie> targets = switch ((int) user.getAbilityValue()) {
            case 1 -> frontBackDetect(user, session);
            case 2 -> areaDetect(user, session);
            case 3 -> waveDetect(user, session);
            case 4 -> swallowDetect(user, session);
            default -> null;
        };
        if(targets == null || targets.isEmpty()) return;
        userAct(user , targets);
        user.setInternalTimer(user.getActionInterval());

    }

    private boolean isEnemyAround(Plant user , GameSession session) {
        Vec2 userPos = user.getPosition();
        switch ((int)user.getAbilityValue()) {
            case 1 : //(front and back)
                for(Zombie zombie : session.getZombies()) {
                    if(zombie != null && zombie.isAlive() && Math.abs(zombie.getPosition().x() - userPos.x()) < 1)
                        return true;
                }
                break;
            case 2 : //(area damage)
                for(Zombie zombie : session.getZombies()) {
                    Vec2 zomPos = zombie.getPosition();
                    if(zombie.isAlive() && Math.abs(zomPos.x() - userPos.x()) < 1 && Math.abs(zomPos.y() - userPos.y()) < 1)
                        return true;
                }
                break;
            case 3 : //(wave damage)
                for(Zombie zombie : session.getZombies()) {
                    if(Math.abs(zombie.getPosition().distanceTo(userPos)) < 5)
                        return true;
                }
                break;
            case 4 : //(swallow)
                for (Zombie zombie : session.getZombies()) {
                    if(zombie.getPosition().x() - userPos.x() < 1)
                        return true;
                }
        }
        return false;
    }

    private ArrayList<Zombie> frontBackDetect(Plant user , GameSession session) {
        Zombie nearestFront = null;
        double shortestFront = Double.MAX_VALUE;
        Zombie nearestBack = null;
        double shortestBack = Double.MAX_VALUE;
        ArrayList<Zombie> targets = new ArrayList<>();

        Vec2 userPos = user.getPosition();
        for(Zombie zombie : session.getZombies()) {
            Vec2 zomPos = zombie.getPosition();
            if(Math.abs(zomPos.y() - userPos.y()) < 0.5) continue;
            if(zomPos.x() - userPos.x() > 0 && zomPos.x() - userPos.x() < 1) {
                double distance = zomPos.distanceTo(userPos);
                if(distance < shortestFront) {
                    shortestFront = distance;
                    nearestFront = zombie;
                }
            }
            else if(zomPos.x() - userPos.x() < 0 && zomPos.x() - userPos.x() > -1) {
                double distance = zomPos.distanceTo(userPos);
                if(distance < shortestBack) {
                    shortestBack = distance;
                    nearestBack = zombie;
                }
            }
        }
        if(nearestFront != null) targets.add(nearestFront);
        if(nearestBack != null) targets.add(nearestBack);
        return targets;
    }

    private ArrayList<Zombie> areaDetect(Plant user , GameSession session) {
        ArrayList<Zombie> targets = new ArrayList<>();
        Vec2 userPos = user.getPosition();
        for(Zombie zombie : session.getZombies()) {
            Vec2 zomPos = zombie.getPosition();
            if(Math.abs(zomPos.y() - userPos.y()) < 1 && Math.abs(zomPos.x() - userPos.x()) < 1)
                targets.add(zombie);
        }
        return targets;
    }

    private ArrayList<Zombie> waveDetect(Plant user , GameSession session) {
        ArrayList<Zombie> targets = new ArrayList<>();
        Vec2 userPos = user.getPosition();
        for(Zombie zombie : session.getZombies()) {
            Vec2 zomPos = zombie.getPosition();
            if(zomPos.distanceTo(userPos) < 5)
                targets.add(zombie);
        }
        return targets;
    }

    private ArrayList<Zombie> swallowDetect(Plant user , GameSession session) {
        ArrayList<Zombie> targets = new ArrayList<>();
        Zombie nearest = null;
        double shortest = Double.MAX_VALUE;

        Vec2 userPos = user.getPosition();
        for(Zombie zombie : session.getZombies()) {
            Vec2 zomPos = zombie.getPosition();
            if(Math.abs(zomPos.y() - userPos.y()) < 0.5) continue;
            if(zomPos.x() - userPos.x() > 0 && zomPos.x() - userPos.x() < 1) {
                double distance = zomPos.distanceTo(userPos);
                if(distance < shortest) {
                    shortest = distance;
                    nearest = zombie;
                }
            }
        }
        if(nearest != null) targets.add(nearest);
        return targets;
    }

    private void userAct(Plant user , ArrayList<Zombie> targets) {
        int userDamage = user.getDamage();
        for(Zombie zombie : targets) {
            zombie.takeDamage(userDamage);
        }
    }
}
