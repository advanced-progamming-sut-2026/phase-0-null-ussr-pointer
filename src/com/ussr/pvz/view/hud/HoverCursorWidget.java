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
        boolean dragging = false;
        if (selectedKey == null) {
            selectedKey = lawnWidget.getDragPreviewKey();
            dragging = selectedKey != null;
        }
        if (selectedKey == null) return;

        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        lawnWidget.screenToLocalCoordinates(mouse);

        int[] cell = lawnWidget.gridCellAt(mouse.x, mouse.y);
        if (cell == null) return;

        float[] pos = lawnWidget.screenPositionForCell(cell[0], cell[1]);
        float drawX = pos[0];
        float drawY = pos[1];
        float cellW = lawnWidget.getCellWidth();
        float cellH = lawnWidget.getCellHeight();

        // TODO-ASSET: image_ui_hud_tile_highlight, replace with real atlas region name
        TextureRegion highlight = textures.region("IMAGE_UI_HUD_TILE_HIGHLIGHT");
        if (highlight != null) {
            Color c = batch.getColor();
            batch.setColor(1f, 1f, 1f, 0.4f * parentAlpha);
            batch.draw(highlight, drawX, drawY, cellW, cellH);
            batch.setColor(c);
        }

        if (!dragging) {
            String packetKey = ChoosePlantService.normalizePlantKey(selectedKey);
            TextureRegion plantReg = textures.region("IMAGE_UI_PACKETS_" + packetKey);
            if (plantReg != null) {
                Color c = batch.getColor();
                batch.setColor(1f, 1f, 1f, 0.6f * parentAlpha);
                batch.draw(plantReg, drawX + cellW * 0.1f, drawY + cellH * 0.1f, cellW * 0.8f, cellH * 0.8f);
                batch.setColor(c);
            }
        }
    }
}