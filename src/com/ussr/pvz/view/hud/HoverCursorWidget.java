package com.ussr.pvz.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.service.ChoosePlantService;
import pvz.libpvz.textures.TextureBank;

/**
 * Tracks the mouse and overlays the selected plant's idle visual snapped to the grid.
 */
public class HoverCursorWidget extends Actor {

    private static final float GRID_OFFSET_X = 320f;
    private static final float GRID_OFFSET_Y = 80f;
    private static final float CELL_WIDTH     = 100f;
    private static final float CELL_HEIGHT    = 115f;
    private static final int   COLUMNS        = 9;
    private static final int   ROWS           = 5;

    private final LawnWidget lawnWidget;
    private final SeedBankHud seedBankHud;
    private final TextureBank textures;

    public HoverCursorWidget(LawnWidget lawnWidget, SeedBankHud seedBankHud, TextureBank textures) {
        this.lawnWidget = lawnWidget;
        this.seedBankHud = seedBankHud;
        this.textures = textures;
        setTouchable(Touchable.disabled); // Pass clicks to the actual LawnWidget
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        String selectedKey = seedBankHud.getSelectedPlantKey();
        if (selectedKey == null) return;

        // Convert screen mouse position to this actor's local coordinate space
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        lawnWidget.screenToLocalCoordinates(mouse);

        // Subtract the grid offset to get position relative to the grid origin
        float relX = mouse.x - GRID_OFFSET_X;
        float relY = mouse.y - GRID_OFFSET_Y;

        // Bail out if mouse is outside the grid area
        if (relX < 0 || relY < 0) return;

        int gridX = (int) (relX / CELL_WIDTH);
        int gridY = (int) (relY / CELL_HEIGHT);

        if (gridX >= COLUMNS || gridY >= ROWS) return;

        // Compute draw position using the same formula as EntityRenderLayer
        float drawX = GRID_OFFSET_X + gridX * CELL_WIDTH;
        float drawY = GRID_OFFSET_Y + gridY * CELL_HEIGHT;

        // Draw tile highlight
        TextureRegion highlight = textures.region("IMAGE_UI_HUD_TILE_HIGHLIGHT");
        if (highlight != null) {
            Color c = batch.getColor();
            batch.setColor(1f, 1f, 1f, 0.4f * parentAlpha);
            batch.draw(highlight, drawX, drawY, CELL_WIDTH, CELL_HEIGHT);
            batch.setColor(c);
        }

        // Draw the plant preview centered in the tile
        String packetKey = ChoosePlantService.normalizePlantKey(selectedKey);
        TextureRegion plantReg = textures.region("IMAGE_UI_PACKETS_" + packetKey);
        if (plantReg != null) {
            Color c = batch.getColor();
            batch.setColor(1f, 1f, 1f, 0.6f * parentAlpha);
            batch.draw(plantReg, drawX + CELL_WIDTH * 0.1f, drawY + CELL_HEIGHT * 0.1f,
                    CELL_WIDTH * 0.8f, CELL_HEIGHT * 0.8f);
            batch.setColor(c);
        }
    }
}