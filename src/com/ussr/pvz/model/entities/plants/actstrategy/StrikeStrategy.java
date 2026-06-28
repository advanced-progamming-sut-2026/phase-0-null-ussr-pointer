package com.ussr.pvz.model.entities.plants.actstrategy;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.PierceHit;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class StrikeStrategy implements ActStrategy {
    @Override
    public void act(Plant user, GameSession session) {
        if(user.getIntervalTimer() <= 0) {
            Zombie target = straightDetect(user , session);
            if(target != null) {
                /*todo : in json file we set the ability value for strike trough plants the number of zombies their projectile pierce
                (-1 for infinite pierce)*/
                int pierceNumber = (int)user.getAbilityValue();
                session.getProjectiles().add(new Projectile(user.getPosition() , new Vec2(0 , 20) , target , user.getDamage() , new StraightMove() , new PierceHit(pierceNumber)));
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
