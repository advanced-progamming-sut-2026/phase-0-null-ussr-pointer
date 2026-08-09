package com.ussr.pvz.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.behavior.BeghouledBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.view.gameplay.LawnGridLayout;
import pvz.libpvz.textures.TextureBank;

/**
 * Draw-only overlay for Beghouled.
 * All input is handled by GameplayController via LawnWidget.
 * This just reads controller state and draws highlights.
 */
public class BeghouledOverlayWidget extends Actor {

    private static final String REGION_HIGHLIGHT = "IMAGE_UI_HUD_TILE_HIGHLIGHT";

    private static final Color COLOR_HOVER    = new Color(1f,   1f,  0.3f, 0.35f);
    private static final Color COLOR_SELECTED = new Color(0.2f, 1f,  0.2f, 0.55f);
    private static final Color COLOR_ADJACENT = new Color(0.2f, 1f,  0.2f, 0.30f);
    private static final Color COLOR_NONADJ   = new Color(1f,  0.2f, 0.2f, 0.30f);

    private final TextureBank         textures;
    private final GameplayController  controller;

    public BeghouledOverlayWidget(TextureBank textures, GameplayController controller) {
        this.textures    = textures;
        this.controller  = controller;
        setTouchable(Touchable.disabled); // LawnWidget handles all input
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        GameSession session = App.getGameSession();
        if (session == null) return;
        LevelBehavior behavior = (LevelBehavior) session.getLevel().getBehavior();
        if (!(behavior instanceof BeghouledBehavior)) return;

        TextureRegion highlight = textures.region(REGION_HIGHLIGHT);
        if (highlight == null) return;

        int selRow = controller.getBeghouledSelectedRow();
        int selCol = controller.getBeghouledSelectedCol();

        Vector2 mouse = getStage().screenToStageCoordinates(
                new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        Color prev = new Color(batch.getColor());

        // 1. Selected cell
        if (selRow >= 0) {
            applyColor(batch, COLOR_SELECTED, parentAlpha);
            batch.draw(highlight,
                    LawnGridLayout.cellX(selCol), LawnGridLayout.cellY(selRow),
                    LawnGridLayout.CELL_WIDTH, LawnGridLayout.CELL_HEIGHT);
        }

        // 2. Hover + adjacency hint
        if (LawnGridLayout.contains(mouse.x, mouse.y)) {
            int hCol = LawnGridLayout.columnAt(mouse.x);
            int hRow = LawnGridLayout.rowAt(mouse.y);
            boolean isSelected = (hRow == selRow && hCol == selCol);

            if (!isSelected) {
                Color c;
                if (selRow >= 0) {
                    int dr = Math.abs(hRow - selRow);
                    int dc = Math.abs(hCol - selCol);
                    c = (dr + dc == 1) ? COLOR_ADJACENT : COLOR_NONADJ;
                } else {
                    c = COLOR_HOVER;
                }
                applyColor(batch, c, parentAlpha);
                batch.draw(highlight,
                        LawnGridLayout.cellX(hCol), LawnGridLayout.cellY(hRow),
                        LawnGridLayout.CELL_WIDTH, LawnGridLayout.CELL_HEIGHT);
            }
        }

        batch.setColor(prev);
    }

    private void applyColor(Batch batch, Color c, float parentAlpha) {
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
    }
}