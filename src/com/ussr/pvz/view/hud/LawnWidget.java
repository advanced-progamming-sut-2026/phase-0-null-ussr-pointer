package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.view.util.WhitePixel;

public class LawnWidget extends Actor {

    private static final int COLUMNS = 9;
    private static final int ROWS = 5;

    // Sync these exactly with EntityRenderLayer
    private static final float GRID_OFFSET_X = 320f;
    private static final float GRID_OFFSET_Y = 80f;
    private static final float CELL_WIDTH = 100f;
    private static final float CELL_HEIGHT = 115f;

    private String dragPreviewKey;

    public LawnWidget(GameplayController controller) {
        setTouchable(Touchable.enabled);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int[] cell = gridCellAt(x, y);
                if (cell != null) {
                    controller.handleGridClick(cell[0], cell[1]);
                }
            }
        });
    }

    public int[] gridCellAt(float localX, float localY) {
        float gridXRaw = localX - GRID_OFFSET_X;
        float gridYRaw = localY - GRID_OFFSET_Y;

        if (gridXRaw < 0 || gridYRaw < 0) {
            return null;
        }

        int gridX = (int) (gridXRaw / CELL_WIDTH);
        int gridY = (int) (gridYRaw / CELL_HEIGHT);

        if (gridX >= COLUMNS || gridY >= ROWS) {
            return null;
        }

        return new int[]{gridX, gridY};
    }

    public float[] screenPositionForCell(int gridX, int gridY) {
        return new float[]{
                getX() + GRID_OFFSET_X + gridX * CELL_WIDTH,
                getY() + GRID_OFFSET_Y + gridY * CELL_HEIGHT
        };
    }

    public float getCellWidth() { return CELL_WIDTH; }
    public float getCellHeight() { return CELL_HEIGHT; }

    public void setDragPreviewKey(String key) {
        this.dragPreviewKey = key;
    }

    public String getDragPreviewKey() {
        return dragPreviewKey;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!DebugOverlay.isGridEnabled()) return;

        TextureRegion pixel = WhitePixel.get();
        Color prev = batch.getColor();

        for (int gy = 0; gy < ROWS; gy++) {
            for (int gx = 0; gx < COLUMNS; gx++) {
                float cellX = getX() + GRID_OFFSET_X + gx * CELL_WIDTH;
                float cellY = getY() + GRID_OFFSET_Y + gy * CELL_HEIGHT;
                drawCellOutline(batch, pixel, cellX, cellY, CELL_WIDTH, CELL_HEIGHT, 1f, 1f, 0f, parentAlpha);
            }
        }

        float fracW = getWidth() / COLUMNS;
        float fracH = getHeight() / ROWS;
        for (int gy = 0; gy < ROWS; gy++) {
            for (int gx = 0; gx < COLUMNS; gx++) {
                float cellX = getX() + gx * fracW;
                float cellY = getY() + gy * fracH;
                drawCellOutline(batch, pixel, cellX, cellY, fracW, fracH, 1f, 0f, 1f, parentAlpha);
            }
        }

        batch.setColor(prev);
    }

    private void drawCellOutline(Batch batch, TextureRegion pixel, float x, float y, float w, float h,
                                 float r, float g, float b, float parentAlpha) {
        float t = 1.5f;
        batch.setColor(r, g, b, 0.9f * parentAlpha);
        batch.draw(pixel, x, y, w, t);            // bottom
        batch.draw(pixel, x, y + h - t, w, t);     // top
        batch.draw(pixel, x, y, t, h);             // left
        batch.draw(pixel, x + w - t, y, t, h);     // right
    }
}