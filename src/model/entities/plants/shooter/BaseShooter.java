package model.entities.plants.shooter;

import model.entities.plants.BasePlant;

public abstract class BaseShooter extends BasePlant {
    private int bulletCount;
    private final ShooterBulletType bulletType;
}

