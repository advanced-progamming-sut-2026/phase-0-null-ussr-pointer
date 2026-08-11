package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.ussr.pvz.view.gameplay.LawnGridLayout;
import com.ussr.pvz.view.util.WhitePixel;

public class LawnGridDebugOverlay extends Actor {

    private static final float LINE_THICKNESS = 2.0f;
    private static final Color GRID_LINE_COLOR = new Color(1.0f, 0.15f, 0.15f, 0.75f);
    private static final Color TEXT_COLOR = new Color(1.0f, 0.9f, 0.3f, 0.9f);

    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    public LawnGridDebugOverlay(Skin skin) {
        setTouchable(Touchable.disabled);
        this.font = skin.has("default", BitmapFont.class)
                ? skin.getFont("default")
                : new BitmapFont();
        this.glyphLayout = new GlyphLayout();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!DebugOverlay.isHitboxEnabled()) {
            return;
        }

        TextureRegion pixel = WhitePixel.get();
        Color oldColor = new Color(batch.getColor());

        // 1. Render Grid Cell Lines
        batch.setColor(GRID_LINE_COLOR.r, GRID_LINE_COLOR.g, GRID_LINE_COLOR.b, GRID_LINE_COLOR.a * parentAlpha);

        for (int row = 0; row < LawnGridLayout.ROWS; row++) {
            for (int col = 0; col < LawnGridLayout.COLUMNS; col++) {
                float cellX = LawnGridLayout.cellX(col);
                float cellY = LawnGridLayout.cellY(row);
                float w = LawnGridLayout.CELL_WIDTH;
                float h = LawnGridLayout.CELL_HEIGHT;

                // Bottom border
                batch.draw(pixel, cellX, cellY, w, LINE_THICKNESS);
                // Top border
                batch.draw(pixel, cellX, cellY + h - LINE_THICKNESS, w, LINE_THICKNESS);
                // Left border
                batch.draw(pixel, cellX, cellY, LINE_THICKNESS, h);
                // Right border
                batch.draw(pixel, cellX + w - LINE_THICKNESS, cellY, LINE_THICKNESS, h);
            }
        }

        // 2. Render Coordinates (Col, Row)
        font.setColor(TEXT_COLOR.r, TEXT_COLOR.g, TEXT_COLOR.b, TEXT_COLOR.a * parentAlpha);
        float originalScaleX = font.getScaleX();
        float originalScaleY = font.getScaleY();
        font.getData().setScale(0.55f);

        for (int row = 0; row < LawnGridLayout.ROWS; row++) {
            for (int col = 0; col < LawnGridLayout.COLUMNS; col++) {
                float cellX = LawnGridLayout.cellX(col);
                float cellY = LawnGridLayout.cellY(row);

                String label = String.format("(%d, %d)", col, row);
                glyphLayout.setText(font, label);

                float textX = cellX + 6.0f;
                float textY = cellY + LawnGridLayout.CELL_HEIGHT - 6.0f;

                font.draw(batch, label, textX, textY);
            }
        }

        font.getData().setScale(originalScaleX, originalScaleY);
        batch.setColor(oldColor);
    }
}