package com.ussr.pvz.view.hud;

import com.ussr.pvz.model.App;

public final class DebugOverlay {
    private DebugOverlay() {}

    public static boolean isGridEnabled()    { return App.isGridEnabled(); }
    public static void    toggleGrid()       { App.setGridEnabled(!App.isGridEnabled()); }

    // Hitbox stays local — it's a pure render toggle with no menu surface
    private static boolean hitboxEnabled = false;
    public static boolean isHitboxEnabled()  { return hitboxEnabled; }
    public static void    toggleHitboxes()   { hitboxEnabled = !hitboxEnabled; }
}