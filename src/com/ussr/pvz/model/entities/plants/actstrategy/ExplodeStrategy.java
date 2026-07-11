package com.ussr.pvz.model.entities.plants.actstrategy;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
public class ExplodeStrategy implements ActStrategy{
    private final double TRAP_ACTIVATION_RADIUS = 0.3;
    @Override
    //todo : if an explosive plant have an activate delay set its activate for its action interval and also do it for interval timer
    //todo : ability values : 1 for (touch) 2 for (area) 3 for (line) 4 for (whole pitch) 5 for (nearest(water of land)
    public void act(Plant user, GameSession session) {
        if(user.getIntervalTimer() > 0) return;
        ArrayList<Zombie> targets = null;
        switch ((int)user.getAbilityValue()) {
            case 1 :
                if(!isZombieTouch(user , session)) return;
                targets = touchDetect(user , session);
                break;
            case 2 :
                targets = areaDetect(user , session);
                break;
            case 3 :
                targets = lineDetect(user , session);
                break;
            case 4 :
                targets = wholePitchDetect(user , session);
                makeHole(user, session); // Pass 'user' here
                break;
        }
        if(targets == null || targets.isEmpty()) return;
        userAct(user , targets);
    }

    private boolean isZombieTouch(Plant user , GameSession session) {
        Vec2 userPos = user.getPosition();
        for(Zombie zombie : session.getZombies()) {
            Vec2 zomPos = zombie.getPosition();
            if(zombie.getPosition().distanceTo(userPos) < TRAP_ACTIVATION_RADIUS)
                return true;
        }
        return false;
    }

    private ArrayList<Zombie> touchDetect(Plant user , GameSession session) {
        ArrayList<Zombie> targets = new ArrayList<>();
        Vec2 userPos = user.getPosition();
        Zombie firstTouch = null;
        double shortest = Double.MAX_VALUE;

        for(Zombie zombie : session.getZombies()) {
            Vec2 zomPos = zombie.getPosition();
            double distance = zomPos.distanceTo(userPos);
            if(distance < shortest) {
                shortest = distance;
                firstTouch = zombie;
            }
        }
        if(firstTouch != null) targets.add(firstTouch);
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

    private ArrayList<Zombie> lineDetect(Plant user , GameSession session) {
        ArrayList<Zombie> targets = new ArrayList<>();
        Vec2 userPos = user.getPosition();
        for(Zombie zombie : session.getZombies()) {
            Vec2 zomPos = zombie.getPosition();
            if(Math.abs(userPos.y() - zomPos.y()) < 0.5) {
                targets.add(zombie);
            }
        }
        return targets;
    }

    private ArrayList<Zombie> wholePitchDetect(Plant user , GameSession session) {
        return (ArrayList<Zombie>) session.getZombies();
    }

    private void makeHole(Plant user, GameSession session) {
        int row = (int) user.getPosition().y();
        int col = (int) user.getPosition().x();

        com.ussr.pvz.model.board.Cell cell = session.getLawn().getCell(row, col);
        if (cell != null) {
            // Replaces the tile with a Crater, preventing planting.
            cell.setTile(new com.ussr.pvz.model.board.terrain.Tile(com.ussr.pvz.model.board.terrain.TileType.Crater));
        }
    }

    private void userAct(Plant user , ArrayList<Zombie> targets) {
        int userDamage = user.getDamage();
        if(user.getTags().contains(Tag.ICE)) {
            for(Zombie zombie : targets) {
                zombie.setStatus(Zombie.Status.FREEZE);
                zombie.takeDamage(userDamage);
            }
        }
        else if(user.getTags().contains(Tag.FIRE)) {
            for (Zombie zombie : targets) {
                zombie.setStatus(Zombie.Status.FIRED);
                zombie.takeDamage(userDamage);
            }
        }
        else {
            for (Zombie zombie : targets)
                zombie.takeDamage(userDamage);
        }
    }

}
