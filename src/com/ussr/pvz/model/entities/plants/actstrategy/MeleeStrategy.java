package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.board.structures.PushableStructure;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;

public class MeleeStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        ArrayList<PushableStructure> structureTargets = nearbyStructures(user, session);
        if (!isEnemyAround(user, session) && structureTargets.isEmpty()) return;

        ArrayList<Zombie> targets = switch ((int) user.getAbilityValue()) {
            case 1 -> frontBackDetect(user, session);
            case 2 -> areaDetect(user, session);
            case 3 -> waveDetect(user, session);
            case 4 -> swallowDetect(user, session);
            default -> null;
        };

        boolean actedOnZombies = targets != null && !targets.isEmpty();
        if (actedOnZombies) {
            userAct(user, targets);
        }
        if (!structureTargets.isEmpty()) {
            userActOnStructures(user, structureTargets);
        }
        if (actedOnZombies || !structureTargets.isEmpty()) {
            user.setInternalTimer(0.0);
        }
    }

    private ArrayList<PushableStructure> nearbyStructures(Plant user, GameSession session) {
        ArrayList<PushableStructure> found = new ArrayList<>();
        int abilityValue = (int) user.getAbilityValue();
        if (abilityValue != 1 && abilityValue != 2) return found;

        Vec2 userPos = user.getPosition();
        for (InteractableStructure structure : session.getLawn().getAllInteractable()) {
            if (!(structure instanceof PushableStructure pushable) || !pushable.isAlive()) continue;
            Vec2 structPos = pushable.getPosition();
            if (structPos == null) continue;
            if (Math.abs(structPos.y() - userPos.y()) < 1 && Math.abs(structPos.x() - userPos.x()) < 1) {
                found.add(pushable);
            }
        }
        return found;
    }

    private void userActOnStructures(Plant user, ArrayList<PushableStructure> structures) {
        int userDamage = user.getDamage();
        for (PushableStructure structure : structures) {
            structure.takeDamage(userDamage);
        }
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
                    if(zombie.isAlive() && Math.abs(zomPos.x() - userPos.x()) < 1 &&
                            Math.abs(zomPos.y() - userPos.y()) < 1)
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
            if(Math.abs(zomPos.y() - userPos.y()) > 0.5) continue;
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
            if(Math.abs(zomPos.y() - userPos.y()) < 1.2 && Math.abs(zomPos.x() - userPos.x()) < 1.2)
                targets.add(zombie);
        }
        return targets;
    }

    private ArrayList<Zombie> waveDetect(Plant user , GameSession session) {
        //todo : we may need to make a wave class for this
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
            if(Math.abs(zomPos.y() - userPos.y()) > 0.5) continue;
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

        // If the ability is 4 (Swallow), ensure an instant kill
        if ((int) user.getAbilityValue() == 4) {
            userDamage = 99999;
        }

        for(Zombie zombie : targets) {
//            System.out.println("helllo");
//            System.out.println("x : " + zombie.getPosition().x() + " y : " + zombie.getPosition().y());
            zombie.takeDamage(userDamage, user);
        }
    }
}