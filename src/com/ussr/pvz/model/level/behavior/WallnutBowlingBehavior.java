package com.ussr.pvz.model.level.behavior;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.projectiles.BowlingNutProjectile;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.util.Vec2;

public class WallnutBowlingBehavior implements LevelBehavior {

    private final int redLineColumn;

    public WallnutBowlingBehavior(int redLineColumn) {
        this.redLineColumn = redLineColumn;
    }

    @Override
    public void onStart(Level level) {
        // Disable normal sun production entirely
        level.setSunFalling(false);
    }

    @Override
    public void onWaveComplete(Level level, int waveNumber) {}

    @Override
    public void onComplete(Level level) {}

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

        BowlingNutProjectile.NutType type;
        switch (nutTypeStr.toUpperCase()) {
            case "EXPLODE_O_NUT": type = BowlingNutProjectile.NutType.EXPLODING; break;
            case "GIANT_WALLNUT": type = BowlingNutProjectile.NutType.GIANT; break;
            default: type = BowlingNutProjectile.NutType.NORMAL; break;
        }

        BowlingNutProjectile nut = new BowlingNutProjectile(Vec2.of(x, y), type);
        session.getProjectiles().add(nut);
        session.getEventBus().subscribe(null, null); // Optionally emit a sound event here

        return "Rolled a " + nutTypeStr + "!";
    }
}