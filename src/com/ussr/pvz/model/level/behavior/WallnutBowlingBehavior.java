package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.projectiles.BowlingNutProjectile;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.delivery.ConveyorDeliveryStrategy;
import com.ussr.pvz.model.level.delivery.DeliveryStrategy;
import com.ussr.pvz.model.util.Vec2;

public class WallnutBowlingBehavior extends LevelBehavior {

    private final int redLineColumn;
    private double timer;
    private double max = 5.0;

    public WallnutBowlingBehavior(int redLineColumn) {
        this.redLineColumn = redLineColumn;
    }

    @Override
    public void onStart(Level level) {
        super.onStart(level);
        DeliveryStrategy strategy = level.getDeliveryStrategy();
        strategy.deliver();
        level.setSunFalling(false);
    }

    @Override
    public void tick(GameSession session, double deltaTime) {
        timer += deltaTime;
        if (timer >= max) {
            session.getLevel().getDeliveryStrategy().deliver();
            timer = 0;
        }
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {
    }

    @Override
    public void onComplete(Level level) {
    }

    @Override
    public boolean isFailed(Level level) {
        // Fails if any zombie crosses the player's defenses (handled by main GameSession)
        return false;
    }


    /**
     * Call this from your GameController instead of standard planting logic
     * during a Wallnut Bowling level.
     */
    public String rollNut(String nutTypeStr, int x, int y) {
        if (x > redLineColumn) {
            return "You cannot place a Wall-nut past the red line!";
        }

        GameSession session = App.getGameSession();
        if (session == null) return "Game session not active.";
        DeliveryStrategy strategy = session.getLevel().getDeliveryStrategy();
        if (strategy instanceof ConveyorDeliveryStrategy) {
            ((ConveyorDeliveryStrategy) strategy).getConveyorBelt().stream().filter(
                    s ->
                            s.equalsIgnoreCase(nutTypeStr)

            ).findFirst().ifPresent(s -> {
                BowlingNutProjectile.NutType type = switch (s.toUpperCase()) {
                    case "EXPLODE-O-NUT" -> BowlingNutProjectile.NutType.EXPLODING;
                    case "GIANT-WALLNUT" -> BowlingNutProjectile.NutType.GIANT;
                    default -> BowlingNutProjectile.NutType.NORMAL;
                };

                BowlingNutProjectile nut = new BowlingNutProjectile(Vec2.of(x, y), type);

                session.addProjectile(nut);
                ((ConveyorDeliveryStrategy) strategy).getConveyorBelt().remove(s);
            });
        }

        return "Rolled a " + nutTypeStr + "!";
    }
}