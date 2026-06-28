package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.NormalHit;
import com.ussr.pvz.model.entities.projectiles.hit.PierceHit;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HomingStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        if(user.getIntervalTimer() <= 0) {
            String name = user.getName();
            if(name.equalsIgnoreCase("Caulipower") || name.equalsIgnoreCase("Electric Blueberry")) {
                Zombie target = randomSelect(user, session);
                if(target == null) return;
                Projectile newShot;
                if(name.equalsIgnoreCase("Caulipower")) {
                    int pierceNumber = (int)user.getAbilityValue();
                    newShot = new Projectile(user.getPosition(), new Vec2(20, 20), target, user.getDamage(), new StraightMove(), new PierceHit(pierceNumber));
                    newShot.setStunning(true);
                }
                else {
                    newShot = new Projectile(user.getPosition(), new Vec2(20, 20), target, user.getDamage(), new StraightMove(), new NormalHit(1));
                }
                session.getProjectiles().add(newShot);
            }
            else if(name.equalsIgnoreCase("Cat-tail")) {
                Zombie target = findNearest(user , session);
                if(target == null) return;
                session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(20 , 20) , target , user.getDamage() , new StraightMove() , new NormalHit(1)));
            }
        }

    }

    private Zombie randomSelect(Plant user , GameSession session) {
        List<Zombie> zombies = session.getZombies();
        if(zombies.isEmpty()) return null;
        int zombiesNumber = zombies.size();
        int random = ThreadLocalRandom.current().nextInt(0 , zombiesNumber);
        return zombies.get(random);
    }

    private Zombie findNearest(Plant user , GameSession session) {
        List<Zombie> zombies = session.getZombies();
        if(zombies.isEmpty()) return null;
        Zombie nearest = null;
        double shortestLength = -1;
        for (Zombie zombie : zombies) {
            double length = zombie.getPosition().sub(user.getPosition()).length();
            if(length < shortestLength || shortestLength < 0) {
                nearest = zombie;
                shortestLength = length;
            }
        }
        return nearest;
    }
}
