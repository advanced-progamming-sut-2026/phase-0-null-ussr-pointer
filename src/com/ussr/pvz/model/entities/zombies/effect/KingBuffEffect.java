package com.ussr.pvz.model.entities.zombies.effect;

import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.entities.zombies.armor.Armor;

public class KingBuffEffect implements EffectStatus {
    private final double delayBetweenKnighting;
    private double timer;

    public KingBuffEffect(double delayBetweenKnighting) {
        this.delayBetweenKnighting = delayBetweenKnighting;
        this.timer = delayBetweenKnighting;
    }

    @Override
    public void effect(Zombie king, GameSession session) {
        if (!king.isAlive()) return;

        timer += GameClock.SECONDS_PER_TICK;
        if (timer >= delayBetweenKnighting) {
            if (applyBuff(king, session)) {
                timer = 0;
            }
        }
    }

    private boolean applyBuff(Zombie king, GameSession session) {
        int kingRow = (int) king.getPosition().y();
        double kingCol = king.getPosition().x();
        return session.getZombies().stream()
                .filter(GameEntity::isAlive)
                .filter(zombie -> zombie != king)
                .filter(zombie -> zombie.getFaction() == king.getFaction())
                .filter(zombie -> !zombie.getName().contains("King"))
                .filter(zombie -> (int) zombie.getPosition().y() == kingRow)
                .filter(zombie -> Math.abs(zombie.getPosition().x() - kingCol) <= 4.0)
                .filter(zombie -> zombie.getArmor() == null || zombie.getArmor().isDestroyed())
                .findFirst()
                .map(targetZombie -> {
                    Armor knightArmor = ZombieFactory.createKnightArmor();
                    targetZombie.setArmor(knightArmor);
                    return true;
                }).orElse(false);
    }
}