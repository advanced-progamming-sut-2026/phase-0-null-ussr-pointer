package com.ussr.pvz.model.engine.session;

import com.ussr.pvz.model.entities.items.CoinDrop;
import com.ussr.pvz.model.entities.items.DiamondDrop;
import com.ussr.pvz.model.entities.zombies.Zombie;

import java.util.Random;

public final class SessionRewardHandler {
    private static final int COIN_CHANCE = 15;
    private static final int DIAMOND_CHANCE = 2;
    private static final int DIAMOND_AMOUNT = 1;

    private final GameSession session;
    private final Random random = new Random();

    public SessionRewardHandler(GameSession session) {
        this.session = session;
    }

    public void rollZombieLoot(Zombie zombie) {
        if (zombie.getPosition() == null) {
            return;
        }

        if (random.nextInt(100) < COIN_CHANCE) {
            addCoinDrop(zombie);
        }

        if (random.nextInt(100) < DIAMOND_CHANCE) {
            addDiamondDrop(zombie);
        }
    }

    private void addCoinDrop(Zombie zombie) {
        CoinDrop drop = new CoinDrop(rollCoinTier());
        drop.setPosition(zombie.getPosition());
        session.addItem(drop);
    }

    private void addDiamondDrop(Zombie zombie) {
        DiamondDrop drop =
                new DiamondDrop(DIAMOND_AMOUNT);

        drop.setPosition(zombie.getPosition());
        session.addItem(drop);
    }

    private CoinDrop.CoinTier rollCoinTier() {
        int result = random.nextInt(100);

        if (result < 70) {
            return CoinDrop.CoinTier.BRONZE;
        }

        if (result < 95) {
            return CoinDrop.CoinTier.SILVER;
        }

        return CoinDrop.CoinTier.GOLD;
    }
}