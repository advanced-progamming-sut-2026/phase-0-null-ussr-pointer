package model.entities.projectiles.hit;

import model.entities.zombies.Zombie;

public interface HitEffectStrategy {
    void apply(Zombie zombie);
}
