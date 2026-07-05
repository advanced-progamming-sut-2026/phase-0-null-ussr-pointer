package com.ussr.pvz.model.entities.plants.plantfood;

import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.hit.HitEffectStrategy;
import com.ussr.pvz.model.entities.projectiles.hit.PierceHit;
import com.ussr.pvz.model.entities.projectiles.hit.PierceKnockBackHit;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy;
import com.ussr.pvz.model.entities.projectiles.move.StraightMove;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

public class KnockBackBlast implements PlantFoodEffect {
    private final int damage;
    private final double knockbackDistance;

    public KnockBackBlast(int damage, double knockbackDistance) {
        this.damage = damage;
        this.knockbackDistance = knockbackDistance;
    }

    @Override
    public void triggerSuperpower(Plant user, GameSession session) {
        Vec2 userPos = user.getPosition();
        Vec2 vel = new Vec2(20 , 0);
        MoveStrategy moveStrategy = new StraightMove();
        HitEffectStrategy hitEffectStrategy = new PierceKnockBackHit(-1 , knockbackDistance);
        Projectile projectile = new Projectile(userPos , vel , null , damage , moveStrategy , hitEffectStrategy);
        session.getProjectiles().add(projectile);
    }

    @Override
    public void applyStatusModifiers(Plant user) {
        
    }

    @Override
    public void tickDurationEffect(Plant user, GameSession session, double deltaTime) {

    }
}
