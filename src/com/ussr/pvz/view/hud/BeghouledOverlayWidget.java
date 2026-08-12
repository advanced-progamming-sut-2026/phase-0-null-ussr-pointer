package com.ussr.pvz.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.behavior.BeghouledBehavior;
import com.ussr.pvz.view.gameplay.LawnGridLayout;
import pvz.libpvz.textures.TextureBank;

/**
 * Draw-only overlay for Beghouled.
 * Extends Widget (not Actor) so it participates in layout and always has
 * a valid size when placed inside a Stack.  All input flows through
 * GameplayController via LawnWidget — this only reads state and draws.
 */
public class BeghouledOverlayWidget extends Widget {

    private static final String REGION_HIGHLIGHT = "IMAGE_UI_HUD_TILE_HIGHLIGHT";

    private static final Color COLOR_HOVER    = new Color(1f,   1f,  0.3f, 0.35f);
    private static final Color COLOR_SELECTED = new Color(0.2f, 1f,  0.2f, 0.55f);
    private static final Color COLOR_ADJACENT = new Color(0.2f, 1f,  0.2f, 0.30f);
    private static final Color COLOR_NONADJ   = new Color(1f,  0.2f, 0.2f, 0.30f);

    private final TextureBank        textures;
    private final GameplayController controller;

    public BeghouledOverlayWidget(TextureBank textures, GameplayController controller) {
        this.textures   = textures;
        this.controller = controller;
        setFillParent(true);
        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Guard: need an active Beghouled session
        GameSession session = App.getGameSession();
        if (session == null || session.getLevel() == null) return;
        if (!(session.getLevel().getBehavior() instanceof BeghouledBehavior)) return;

        TextureRegion highlight = textures.region(REGION_HIGHLIGHT);
        if (highlight == null) return;

        int selRow = controller.getBeghouledSelectedRow();
        int selCol = controller.getBeghouledSelectedCol();

        // Convert OS screen coords → stage coords.
        // screenToStageCoordinates handles the Y-flip (screen Y is top-down,
        // stage Y is bottom-up) so the result matches LawnGridLayout directly.
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        if (getStage() != null) {
            getStage().screenToStageCoordinates(mouse);
        }

        Color prev = new Color(batch.getColor());

        // ── 1. Selected cell ─────────────────────────────────────────────────
        if (selRow >= 0) {
            tint(batch, COLOR_SELECTED, parentAlpha);
            drawCell(batch, highlight, selCol, selRow);
        }

        // ── 2. Hovered cell with adjacency hint ──────────────────────────────
        if (LawnGridLayout.contains(mouse.x, mouse.y)) {
            int hCol = LawnGridLayout.columnAt(mouse.x);
            int hRow = LawnGridLayout.rowAt(mouse.y);

            // Don't double-draw the selected cell
            if (hRow != selRow || hCol != selCol) {
                Color c;
                if (selRow >= 0) {
                    int dr = Math.abs(hRow - selRow);
                    int dc = Math.abs(hCol - selCol);
                    c = (dr + dc == 1) ? COLOR_ADJACENT : COLOR_NONADJ;
                } else {
                    c = COLOR_HOVER;
                }
                tint(batch, c, parentAlpha);
                drawCell(batch, highlight, hCol, hRow);
            }
        }

        batch.setColor(prev);
    }

    private void drawCell(Batch batch, TextureRegion region, int col, int row) {
        batch.draw(region,
                LawnGridLayout.cellX(col),
                LawnGridLayout.cellY(row),
                LawnGridLayout.CELL_WIDTH,
                LawnGridLayout.CELL_HEIGHT);
    }

    private void tint(Batch batch, Color c, float parentAlpha) {
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
    }
}