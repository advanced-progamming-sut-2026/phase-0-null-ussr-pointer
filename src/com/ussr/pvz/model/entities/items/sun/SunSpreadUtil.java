package com.ussr.pvz.model.entities.items.sun;


public final class SunSpreadUtil {

    private static final float RADIUS_X = 24f;
    private static final float RADIUS_Y = 14f;

    private SunSpreadUtil() {
    }

    public static float[] offsetFor(int index, int count) {
        if (count <= 1) {
            return new float[]{0f, 0f};
        }
        double angle = (2 * Math.PI * index) / count;
        float offsetX = (float) (RADIUS_X * Math.cos(angle));
        float offsetY = (float) (RADIUS_Y * Math.sin(angle));
        return new float[]{offsetX, offsetY};
    }
}