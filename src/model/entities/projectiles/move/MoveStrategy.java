package model.entities.projectiles.move;

import model.entities.projectiles.Projectile;

public interface MoveStrategy {
    void move(Projectile projectile);
}
