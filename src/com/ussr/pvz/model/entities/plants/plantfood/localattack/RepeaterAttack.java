package com.ussr.pvz.model.entities.plants.plantfood.localattack;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodEffect;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.NormalHit;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.util.Vec2;

public class RepeaterAttack extends LocalAttack implements PlantFoodEffect{

    public RepeaterAttack(double duration, double strikeRate) {
        super(duration, strikeRate);
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        super.triggerSuperpower(user, session);

        if (user == null || session == null) return;

        int giantPeaDamage = user.getDamage() * 20;
        session.addProjectile(new Projectile(user.getPosition(),
                new Vec2(4 , 0),
                null,
                giantPeaDamage,
                new StraightMove(),
                new NormalHit(1)));
    }

    
}
