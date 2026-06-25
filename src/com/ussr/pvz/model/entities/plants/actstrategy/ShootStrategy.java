package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.Tag;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.FireHit;
import com.ussr.pvz.model.entities.projectiles.hit.IceHit;
import com.ussr.pvz.model.entities.projectiles.hit.NormalHit;
import com.ussr.pvz.model.entities.projectiles.move.BounceMove;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.util.Vec2;

import java.rmi.server.ServerNotActiveException;

public class ShootStrategy implements ActStrategy {

    @Override
    public void act(Plant user, GameSession session) {
        //todo change the json file for shooters to has a vec2 for each projectile they shoot
        if(user.getIntervalTimer() <= 0) {
            if(user.getTags().contains(Tag.FIRE)){
                for(Vec2 vec : user.getShootingVectors()) {
                    session.getItems().add(new Projectile(user.getPosition() , vec , user.getDamage() , new StraightMove() , new FireHit(1)));
                }
            }
            else if(user.getTags().contains(Tag.ICE)) {
                for(Vec2 vec : user.getShootingVectors()) {
                    session.getItems().add(new Projectile(user.getPosition() , vec , user.getDamage() , new StraightMove() , new IceHit(1)));
                }
            }
            else if(user.getName().equalsIgnoreCase("bowling bulb")) {
                for(Vec2 vec : user.getShootingVectors()) {
                    session.getItems().add(new Projectile(user.getPosition() , vec , user.getDamage() , new BounceMove() , new NormalHit(1)));
                }
            }
            else {
                for(Vec2 vec : user.getShootingVectors()) {
                    session.getItems().add(new Projectile(user.getPosition() , vec , user.getDamage() , new StraightMove() , new NormalHit(1)));
                }
            }

        }
        user.setInternalTimer(user.getActionInterval());
    }
}
