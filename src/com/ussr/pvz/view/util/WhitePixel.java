package com.ussr.pvz.view.util;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class WhitePixel {
    private static TextureRegion region;

    public static TextureRegion get() {
        if (region == null) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(1f, 1f, 1f, 1f);
            pm.fill();
            region = new TextureRegion(new Texture(pm));
            pm.dispose();
        }
        return region;
    }

    private WhitePixel() {}
}