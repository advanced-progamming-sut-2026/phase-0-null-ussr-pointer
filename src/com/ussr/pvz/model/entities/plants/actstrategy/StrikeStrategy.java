package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.PierceHit;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.util.Vec2;

public class StrikeStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        if(user.getIntervalTimer() <= 0) {
            session.getItems().add(new Projectile(user.getPosition() , new Vec2(1 , 0) , user.getDamage() , new StraightMove() , new PierceHit((int)user.getAbilityValue())));
            user.setInternalTimer(user.getActionInterval());
        }
    }
}
