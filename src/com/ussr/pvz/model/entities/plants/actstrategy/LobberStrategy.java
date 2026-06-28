package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.FireHit;
import com.ussr.pvz.model.entities.projectiles.hit.IceHit;
import com.ussr.pvz.model.entities.projectiles.hit.NormalHit;
import com.ussr.pvz.model.entities.projectiles.hit.PoisonHit;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class LobberStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        if(user.getIntervalTimer() <= 0) {
            String name = user.getName();
            Zombie target = straightDetect(user , session);
            if(target != null) {
                int areaLength = 1;
                if(user.getTags().contains(Tag.AOE)) areaLength = 3;
                if(user.getTags().contains(Tag.FIRE))
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(20 , 20) , target , user.getDamage() , new ArcMove() , new FireHit(areaLength)));
                else if(user.getTags().contains(Tag.ICE))
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(20 , 20) , target , user.getDamage() , new ArcMove() , new IceHit(areaLength)));
                else if(user.getTags().contains(Tag.POISON))
                    session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(20 , 20) , target , user.getDamage() , new ArcMove() , new PoisonHit(areaLength)));
            }
        }
    }

    private Zombie straightDetect(Plant user , GameSession session) {
        for (Zombie zombie : session.getZombies()) {
            if(zombie != null) {
                Vec2 itemPos = zombie.getPosition();
                Vec2 userPos = user.getPosition();
                if (itemPos.x() == userPos.x() && itemPos.y() > userPos.y())
                    return zombie;
            }
        }
        return null;
    }
}
