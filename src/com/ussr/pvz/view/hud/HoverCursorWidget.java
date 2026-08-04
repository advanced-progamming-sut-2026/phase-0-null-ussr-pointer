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
        if (selectedKey == null) return;

        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        lawnWidget.screenToLocalCoordinates(mouse);

        if (mouse.x < 0 || mouse.x > lawnWidget.getWidth() || mouse.y < 0 || mouse.y > lawnWidget.getHeight()) {
            return;
        }

        float cellW = lawnWidget.getWidth() / 9f;
        float cellH = lawnWidget.getHeight() / 5f;
        int gridX = (int) (mouse.x / cellW);
        int gridY = (int) (mouse.y / cellH);

        float drawX = lawnWidget.getX() + gridX * cellW;
        float drawY = lawnWidget.getY() + gridY * cellH;

        // Draw tile grid highlight
        // TODO-ASSET: image_ui_hud_tile_highlight, replace with real atlas region name
        TextureRegion highlight = textures.region("IMAGE_UI_HUD_TILE_HIGHLIGHT");
        if (highlight != null) {
            Color c = batch.getColor();
            batch.setColor(1f, 1f, 1f, 0.4f * parentAlpha);
            batch.draw(highlight, drawX, drawY, cellW, cellH);
            batch.setColor(c);
        }

        // Draw the plant preview
        String packetKey = ChoosePlantService.normalizePlantKey(selectedKey);
        TextureRegion plantReg = textures.region("IMAGE_UI_PACKETS_" + packetKey);
        if (plantReg != null) {
            Color c = batch.getColor();
            batch.setColor(1f, 1f, 1f, 0.6f * parentAlpha); // Semi-transparent hover
            batch.draw(plantReg, drawX + cellW * 0.1f, drawY + cellH * 0.1f, cellW * 0.8f, cellH * 0.8f);
            batch.setColor(c);
        }
    }
}