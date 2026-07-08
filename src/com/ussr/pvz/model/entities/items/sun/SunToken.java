package com.ussr.pvz.model.entities.items.sun;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;

import java.util.Random;

public class SunToken extends GroundItem {
    private static final Random RAND = new Random();
    private static final double SECONDS_TO_FALL = 5.0;
    private static final int TOTAL_FALL_TICKS = (int) (SECONDS_TO_FALL / 0.1);

    private final SunDropType dropType;
    private boolean falling;

    private final int targetRow;
    private final int targetCol;
    private double currentY;
    private final double fallTargetY;
    private int elapsedTicks = 0;

    public SunToken(int maxRows, int maxCols) {
        super(ItemType.SUN, 40f, 20f);
        int roll = RAND.nextInt(100);
        SunDropType selectedType = SunDropType.REGULAR;
        int cumulativeProbability = 0;

        for (SunDropType type : SunDropType.values()) {
            cumulativeProbability += type.getProbability();
            if (roll < cumulativeProbability) {
                selectedType = type;
                break;
            }
        }
        this.dropType = selectedType;

        this.targetRow = RAND.nextInt(maxRows);
        this.targetCol = RAND.nextInt(maxCols);

        this.falling = true;
        this.currentY = 0.0;
        this.fallTargetY = targetRow;
        App.getGameSession().getEventBus()
                .publish(new GameEvent.SunStartedFalling(selectedType.toString(),targetRow,targetCol));
    }

    @Override
    public void applyRewards(GameSession session, Account account) {
        if (dropType != SunDropType.SPECIAL) {
            session.addSun(dropType.getValue());
            App.getGameSession().getEventBus().publish(new GameEvent.SunCollected(dropType.getValue(), App.getGameSession().getSunCount()));
        } else {
            if (falling) {
                explodeRadioactive(session);
                this.isAlive = false;
                this.setCollected(true);
                return;
            }
            session.addSun(dropType.getValue());
            App.getGameSession().getEventBus().publish(new GameEvent.SunCollected(dropType.getValue(), App.getGameSession().getSunCount()));
        }
        this.isAlive = false;
        this.setCollected(true);
    }

    private void explodeRadioactive(GameSession session) {
        int rCenter = targetRow;
        int cCenter = targetCol;

        // Deal 150 damage to zombies in a 5x5 area (Radius of 2)
        if (session.getZombies() != null) {
            for (com.ussr.pvz.model.entities.zombies.Zombie zombie : session.getZombies()) {
                if (!zombie.isAlive()) continue;

                double zY = zombie.getPosition().y();
                double zX = zombie.getPosition().x();

                if (Math.abs(zY - rCenter) <= 2 && Math.abs(zX - cCenter) <= 2) {
                    zombie.takeDamage(150);
                }
            }
        }

        // Deal 80 damage to plants in a 3x3 area (Radius of 1)
        if (session.getPlants() != null) {
            for (com.ussr.pvz.model.entities.plants.Plant plant : session.getPlants()) {
                if (!plant.isAlive()) continue;

                int pY = plant.getLocation().y();
                int pX = plant.getLocation().x();

                if (Math.abs(pY - rCenter) <= 1 && Math.abs(pX - cCenter) <= 1) {
                    // Pass null for the dealer, or create a specific environmental damage source if needed
                    plant.takeDamage(80, null);
                }
            }
        }
    }

    @Override
    public void tick() {
        if (falling) {
            elapsedTicks++;

            double progress = (double) elapsedTicks / TOTAL_FALL_TICKS;
            currentY = progress * fallTargetY;


            if (elapsedTicks >= TOTAL_FALL_TICKS) {
                falling = false;
                currentY = fallTargetY;
                App.getGameSession().getEventBus().publish(new GameEvent.SunGrounded(targetRow,targetCol));
            }
        }
    }

    public SunDropType getDropType() {
        return dropType;
    }

    public int getValue() {
        return dropType.getValue();
    }

    public boolean isFalling() {
        return falling;
    }

    public void setFalling(boolean falling) {
        this.falling = falling;
    }

    public double getCurrentY() {
        return currentY;
    }

    public int getTargetRow() {
        return targetRow;
    }

    public int getTargetCol() {
        return targetCol;
    }
}