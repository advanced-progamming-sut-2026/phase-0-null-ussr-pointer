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
        super.tick(session, deltaTime);

        if (session.isGameOver() || levelCompleted) return;

        timer += deltaTime;
        if (timer >= max) {
            session.getLevel().getDeliveryStrategy().deliver();
            timer = 0;
        }
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

        if (session.getLawn() == null || y < 0 || y >= session.getLawn().getRows()) {
            return "Invalid row for rolling a nut.";
        }

        DeliveryStrategy strategy = session.getLevel().getDeliveryStrategy();
        if (!(strategy instanceof ConveyorDeliveryStrategy conveyorStrategy)) {
            return "Current level has no conveyor belt to roll from.";
        }

        java.util.Optional<String> matched = conveyorStrategy.getConveyorBelt().stream()
                .filter(s -> s.equalsIgnoreCase(nutTypeStr))
                .findFirst();

        if (matched.isEmpty()) {
            return "No \"" + nutTypeStr + "\" available on the conveyor belt.";
        }

        String belt = matched.get();
        BowlingNutProjectile.NutType type = switch (belt.toUpperCase()) {
            case "EXPLODE-O-NUT" -> BowlingNutProjectile.NutType.EXPLODING;
            case "GIANT-WALLNUT" -> BowlingNutProjectile.NutType.GIANT;
            default -> BowlingNutProjectile.NutType.NORMAL;
        };

        BowlingNutProjectile nut = new BowlingNutProjectile(Vec2.of(x, y), type);
        session.addProjectile(nut);
        conveyorStrategy.getConveyorBelt().remove(belt);

        return "Rolled a " + belt + "!";
    }
}