package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.FireHit;
import com.ussr.pvz.model.entities.projectiles.hit.IceHit;
import com.ussr.pvz.model.entities.projectiles.hit.NormalHit;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.util.Vec2;

public class LobberStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        if(user.getIntervalTimer() <= 0) {
            int areaLength = 1;
            if(user.getTags().contains(Tag.AOE))
                areaLength = 3;
            if(user.getTags().contains(Tag.FIRE))
                session.getItems().add(new Projectile(user.getPosition() , new Vec2(1 , 0) , user.getDamage(), new ArcMove() , new FireHit(areaLength)));
            else if (user.getTags().contains(Tag.ICE))
                session.getItems().add(new Projectile(user.getPosition() , new Vec2(1 , 0) , user.getDamage(), new ArcMove() , new IceHit(areaLength)));
            else
                session.getItems().add(new Projectile(user.getPosition() , new Vec2(1 , 0) , user.getDamage() , new ArcMove() , new NormalHit(areaLength)));
        }

    }
}
