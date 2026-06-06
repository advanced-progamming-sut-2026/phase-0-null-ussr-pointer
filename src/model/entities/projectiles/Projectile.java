package model.entities.projectiles;

import model.entities.projectiles.hit.HitEffectStrategy;
import model.entities.projectiles.move.MoveStrategy;

public abstract class Projectile extends model.engine.GameEntity {
    private final int direction; //1 for plant -1 for zombies
    private final int damage;

    private final MoveStrategy moveStrategy;
    private HitEffectStrategy hitEffectStrategy;
}


