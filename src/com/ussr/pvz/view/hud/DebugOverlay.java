package com.ussr.pvz.view.hud;

public final class DebugOverlay {
    private static boolean gridEnabled = false;
    private static boolean hitboxEnabled = false;

    private DebugOverlay() {}

    public static boolean isGridEnabled() { return gridEnabled; }
    public static void toggleGrid() { gridEnabled = !gridEnabled; }

    public static boolean isHitboxEnabled() { return hitboxEnabled; }
    public static void toggleHitboxes() { hitboxEnabled = !hitboxEnabled; }
}