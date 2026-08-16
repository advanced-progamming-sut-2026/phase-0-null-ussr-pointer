package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.view.hud.DebugOverlay;
import com.ussr.pvz.view.util.WhitePixel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Draws the actual occupied-cell footprint (rows x cols) of every live
 * Zomboss on the lawn, gated on the existing "Hitbox" debug button.
 *
 * The built-in scene2d stage.setDebugAll(...) toggle only outlines rendered
 * Actors, and Zomboss mirror zombies are intentionally never rendered as
 * separate actors (see EntityRenderLayer#syncZombies), so it can never show
 * the boss's real 2x2/3x3 hitbox. This overlay reads the same
 * ZombossController#getOccupiedRows()/getOccupiedColumns() the game logic
 * itself uses, so what's drawn always matches what can actually be hit.
 */
public class ZombossHitboxDebugOverlay extends Actor {

    private static final Color HITBOX_FILL = new Color(1.0f, 0.35f, 0.0f, 0.30f);
    private static final Color HITBOX_BORDER = new Color(1.0f, 0.55f, 0.0f, 0.9f);
    private static final float BORDER_THICKNESS = 2.5f;

    public ZombossHitboxDebugOverlay() {
        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!DebugOverlay.isHitboxEnabled()) return;

        GameSession session = App.getGameSession();
        if (session == null) return;

        TextureRegion pixel = WhitePixel.get();
        Color oldColor = new Color(batch.getColor());

        // A boss's primary + mirrors all share one controller — draw each
        // controller's footprint once, keyed by identity to avoid duplicates.
        Set<ZombossController> drawn = new HashSet<>();

        for (Zombie zombie : session.getZombies()) {
            ZombossController controller = zombie.getZombossController();
            if (controller == null || !controller.getPrimary().isAlive()) continue;
            if (!drawn.add(controller)) continue;

            List<Integer> rows = controller.getOccupiedRows();
            List<Integer> cols = controller.getOccupiedColumns();

            for (int row : rows) {
                if (row < 0 || row >= LawnGridLayout.ROWS) continue;
                for (int col : cols) {
                    if (col < 0 || col >= LawnGridLayout.COLUMNS) continue;

                    float cellX = LawnGridLayout.cellX(col);
                    float cellY = LawnGridLayout.cellY(row);
                    float w = LawnGridLayout.CELL_WIDTH;
                    float h = LawnGridLayout.CELL_HEIGHT;

                    batch.setColor(HITBOX_FILL.r, HITBOX_FILL.g, HITBOX_FILL.b, HITBOX_FILL.a * parentAlpha);
                    batch.draw(pixel, cellX, cellY, w, h);

                    batch.setColor(HITBOX_BORDER.r, HITBOX_BORDER.g, HITBOX_BORDER.b, HITBOX_BORDER.a * parentAlpha);
                    batch.draw(pixel, cellX, cellY, w, BORDER_THICKNESS);
                    batch.draw(pixel, cellX, cellY + h - BORDER_THICKNESS, w, BORDER_THICKNESS);
                    batch.draw(pixel, cellX, cellY, BORDER_THICKNESS, h);
                    batch.draw(pixel, cellX + w - BORDER_THICKNESS, cellY, BORDER_THICKNESS, h);
                }
            }
        }

        batch.setColor(oldColor);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        setVisible(DebugOverlay.isHitboxEnabled());
    }
}