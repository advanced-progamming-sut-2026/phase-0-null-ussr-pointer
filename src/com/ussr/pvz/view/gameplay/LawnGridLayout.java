package com.ussr.pvz.view.gameplay;

public final class LawnGridLayout {
    public static final int COLUMNS = 9;
    public static final int ROWS = 5;

    public static final float OFFSET_X = 320f;
    public static final float OFFSET_Y = 80f;
    public static final float CELL_WIDTH = 100f;
    public static final float CELL_HEIGHT = 115f;

    public static final float PLANT_DRAW_OFFSET_X = -40f;
    public static final float PLANT_DRAW_OFFSET_Y = -30f;

    public static final float ZOMBIE_DRAW_OFFSET_X = -40f;
    public static final float ZOMBIE_DRAW_OFFSET_Y = -30f;

    public static final float MOWER_DRAW_OFFSET_X = -60f;
    public static final float MOWER_DRAW_OFFSET_Y = -60f;

    public static final float GRAVE_DRAW_OFFSET_X = -0f;
    public static final float GRAVE_DRAW_OFFSET_Y = -60f;

    public static final float TILE_DRAW_OFFSET_X = 20f;   // nudge right
    public static final float TILE_DRAW_OFFSET_Y = -90f;  // nudge down

    public static final float SLIPPERY_DRAW_OFFSET_X = TILE_DRAW_OFFSET_X;
    public static final float SLIPPERY_DRAW_OFFSET_Y = TILE_DRAW_OFFSET_Y + 40f;

    private LawnGridLayout() {
    }

    public static int columnAt(float x) {
        return (int) ((x - OFFSET_X) / CELL_WIDTH);
    }

    public static int rowAt(float y) {
        return (int) ((y - OFFSET_Y) / CELL_HEIGHT);
    }

    public static boolean contains(float x, float y) {
        return x >= OFFSET_X
                && x < OFFSET_X + COLUMNS * CELL_WIDTH
                && y >= OFFSET_Y
                && y < OFFSET_Y + ROWS * CELL_HEIGHT;
    }

    public static float cellX(int column) {
        return OFFSET_X + column * CELL_WIDTH;
    }

    public static float cellY(int row) {
        return OFFSET_Y + row * CELL_HEIGHT;
    }

    public static float worldX(double column) {
        return OFFSET_X + (float) column * CELL_WIDTH;
    }

    public static float worldY(double row) {
        return OFFSET_Y + (float) row * CELL_HEIGHT;
    }

    public static float centeredActorX(
            int column,
            float actorWidth
    ) {
        return cellX(column)
                + (CELL_WIDTH - actorWidth) / 2f;
    }

    public static float centeredActorY(
            int row,
            float actorHeight
    ) {
        return cellY(row)
                + (CELL_HEIGHT - actorHeight) / 2f;
    }
}
