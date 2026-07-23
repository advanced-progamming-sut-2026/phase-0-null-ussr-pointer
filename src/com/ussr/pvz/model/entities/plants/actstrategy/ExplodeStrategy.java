package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.InteractableStructure;
import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.PierceHit;
import com.ussr.pvz.model.entities.projectiles.move.BounceMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;

public class ExplodeStrategy implements ActStrategy {
    private final double TRAP_ACTIVATION_RADIUS = 1;

    @Override
    public void act(Plant user, GameSession session) {

        // Arming Delay logic: If it's a delayed explosive (like Potato Mine)
        // and hasn't matured yet, use the action interval as its arming countdown.
        if (user.getTags().contains(Tag.DELAYED) && user.getCurrentStage() < 2) {
            return;
        }

        ArrayList<Zombie> targets = null;
        switch ((int) user.getAbilityValue()) {
            case 1:
                if (!isZombieTouch(user, session)) return;
                targets = touchDetect(user, session);
                break;
            case 2:
                targets = areaDetect(user, session);
                if(user.getName().equalsIgnoreCase("grapeshot"))
                    App.getGameSession().addProjectile(new Projectile(user.getPosition(),
                            new Vec2(4 , 0),
                            null,
                            50,
                            new BounceMove(),
                            new PierceHit(Integer.MAX_VALUE)));
                break;
            case 3:
                targets = lineDetect(user, session);
                break;
            case 4:
                targets = wholePitchDetect(user, session);
                makeHole(user, session);
                break;
            case 5: // Complete the nearest target functionality
                targets = nearestZombieDetect(user, session);
                break;
            case 6:
                handleGraveDestroy(user , session);
                break;
            case 7:
                handleFreezeTileDestroy(user , session);
        }

        if (targets == null || targets.isEmpty()) return;
        userAct(user, targets);
        user.setAlive(false); // Detonate and clear the plant entity
    }

    private boolean isZombieTouch(Plant user, GameSession session) {
        Vec2 userPos = user.getPosition();
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive() && zombie.getPosition().distanceTo(userPos) < TRAP_ACTIVATION_RADIUS) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Zombie> touchDetect(Plant user, GameSession session) {
        ArrayList<Zombie> targets = new ArrayList<>();
        Vec2 userPos = user.getPosition();
        Zombie firstTouch = null;
        double shortest = Double.MAX_VALUE;

        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) continue;
            double distance = zombie.getPosition().distanceTo(userPos);
            if (distance < shortest && distance < TRAP_ACTIVATION_RADIUS) {
                shortest = distance;
                firstTouch = zombie;
            }
        }
        if (firstTouch != null) targets.add(firstTouch);
        return targets;
    }

    private ArrayList<Zombie> areaDetect(Plant user, GameSession session) {
        ArrayList<Zombie> targets = new ArrayList<>();
        Vec2 userPos = user.getPosition();
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) continue;
            Vec2 zomPos = zombie.getPosition();
            if (Math.abs(zomPos.y() - userPos.y()) <= 1.5 && Math.abs(zomPos.x() - userPos.x()) <= 1.5) {
                targets.add(zombie);
            }
        }
        return targets;
    }

    private ArrayList<Zombie> lineDetect(Plant user, GameSession session) {
        ArrayList<Zombie> targets = new ArrayList<>();
        Vec2 userPos = user.getPosition();
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) continue;
            Vec2 zomPos = zombie.getPosition();
            if (Math.abs(userPos.y() - zomPos.y()) < 0.5) {
                targets.add(zombie);
            }
        }
        return targets;
    }

    private ArrayList<Zombie> wholePitchDetect(Plant user, GameSession session) {
        ArrayList<Zombie> targets = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive()) targets.add(zombie);
        }
        return targets;
    }

    private ArrayList<Zombie> nearestZombieDetect(Plant user, GameSession session) {
        ArrayList<Zombie> targets = new ArrayList<>();
        Zombie nearest = null;
        double shortest = Double.MAX_VALUE;
        Vec2 userPos = user.getPosition();

        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) continue;
            double dist = zombie.getPosition().distanceTo(userPos);
            if (dist < shortest) {
                shortest = dist;
                nearest = zombie;
            }
        }
        if (nearest != null) targets.add(nearest);
        return targets;
    }

    private void makeHole(Plant user, GameSession session) {
        int row = (int) user.getPosition().y();
        int col = (int) user.getPosition().x();
        com.ussr.pvz.model.board.Cell cell = session.getLawn().getCell(row, col);
        if (cell != null) {
            cell.setTile(new com.ussr.pvz.model.board.terrain.Tile(com.ussr.pvz.model.board.terrain.TileType.Crater));
        }
    }

    private void userAct(Plant user, ArrayList<Zombie> targets) {
        int userDamage = user.getDamage();
        for (Zombie zombie : targets) {
            if (user.getTags().contains(Tag.ICE)) {
                zombie.setStatus(Zombie.Status.FREEZE);
            } else if (user.getTags().contains(Tag.FIRE)) {
                zombie.setStatus(Zombie.Status.FIRED);
            }
            zombie.takeDamage(userDamage, user);
        }
    }

    private void handleGraveDestroy(Plant user , GameSession session) {
        Vec2 userPos = user.getPosition();

        InteractableStructure structure = session.getLawn().getCell((int) userPos.y() ,
                (int) userPos.x()).getInteractableStructure();
        if(structure != null) {
            structure.setAlive(false);
            session.getEventBus().publish(new GameEvent.StructureDestroyed(structure.toString() ,
                    (int) structure.getPosition().y() , (int) structure.getPosition().x()));
        }
    }

    private void handleFreezeTileDestroy(Plant user , GameSession session) {
        Vec2 userPos = user.getPosition();

        boolean isFrozen = session.getLawn().getCell((int) userPos.y() ,
                (int) userPos.x()).getTile().getType() == TileType.Frozen;
        if(isFrozen) {
            session.getLawn().getCell((int) userPos.y() ,
                    (int) userPos.x()).getTile().setType(TileType.Normal);
        }
    }
}