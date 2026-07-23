package com.ussr.pvz.model.entities.plants.plantfood.localattack;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.NormalHit;
import com.ussr.pvz.model.entities.projectiles.hit.PierceHit;
import com.ussr.pvz.model.entities.projectiles.move.BounceMove;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.util.Vec2;

public class BowlingBulbAttack extends LocalAttack {

    public BowlingBulbAttack() {
        super(0.0, 0.0);
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        super.triggerSuperpower(user, session);
        int damage = user.getDamage();
        session.addProjectile(new Projectile(user.getPosition(),
                new Vec2(4 , 0),
                null,
                damage * 10,
                new BounceMove(),
                new PierceHit(Integer.MAX_VALUE)));

        session.addProjectile(new Projectile(user.getPosition(),
                new Vec2(4 , 0),
                null,
                damage * 15,
                new BounceMove(),
                new PierceHit(Integer.MAX_VALUE)));

        session.addProjectile(new Projectile(user.getPosition(),
                new Vec2(4 , 0),
                null,
                damage * 20,
                new BounceMove(),
                new PierceHit(Integer.MAX_VALUE)));
    }
}
