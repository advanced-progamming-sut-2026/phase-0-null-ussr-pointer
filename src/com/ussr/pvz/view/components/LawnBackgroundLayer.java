package com.ussr.pvz.view.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import pvz.libpvz.textures.TextureBank;

public class LawnBackgroundLayer extends Actor {

    private final TextureRegion leftRegion;
    private final TextureRegion mainRegion;
    private final TextureRegion rightRegion;

    private boolean showRight = false;

    private LawnBackgroundLayer(TextureBank textures, String mainRegionKey) {
        setTouchable(Touchable.disabled);
        this.mainRegion = resolveRegion(textures, mainRegionKey);
        this.leftRegion = resolveSideRegion(textures, mainRegionKey, "_LEFT");
        this.rightRegion = resolveSideRegion(textures, mainRegionKey, "_RIGHT");
    }

    public static LawnBackgroundLayer forGameplay(TextureBank textures, String mainRegionKey, float unusedPanelWidth) {
        return new LawnBackgroundLayer(textures, mainRegionKey);
    }

    public static LawnBackgroundLayer forMenuPreview(TextureBank textures, String mainRegionKey) {
        return new LawnBackgroundLayer(textures, mainRegionKey);
    }

    public void setShowRight(boolean showRight) {
        this.showRight = showRight;
    }

    public boolean isShowRight() {
        return showRight;
    }

    public boolean hasRightRegion() {
        return rightRegion != null;
    }

    private static TextureRegion resolveSideRegion(TextureBank textures, String mainKey, String sideSuffix) {
        if (mainKey == null) return null;

        String baseKey = mainKey.endsWith("_TEXTURE")
                ? mainKey.substring(0, mainKey.length() - "_TEXTURE".length())
                : mainKey;

        TextureRegion region = textures.region(baseKey + sideSuffix + "_TEXTURE");
        if (region != null) return region;

        region = textures.region(baseKey + sideSuffix);
        if (region != null) return region;

        return textures.region(mainKey + sideSuffix);
    }

    private static TextureRegion resolveRegion(TextureBank textures, String key) {
        if (key == null) return null;
        TextureRegion region = textures.region(key);
        if (region == null) {
            System.err.println("[LawnBackgroundLayer] Warning: Missing main texture region for " + key);
        }
        return region;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float destWidth = getWidth();
        float destHeight = getHeight();
        if (destWidth <= 0f || destHeight <= 0f || mainRegion == null) {
            return;
        }

        drawInActorCoordinates(batch, destWidth, destHeight);
    }

    private void drawInActorCoordinates(Batch batch, float destWidth, float destHeight) {
        float sourceLeftWidth = leftRegion != null ? leftRegion.getRegionWidth() : 0f;
        float sourceMainWidth = mainRegion.getRegionWidth();
        float sourceRightWidth =
                showRight && rightRegion != null ? rightRegion.getRegionWidth() : 0f;

        float sourceWidth = sourceLeftWidth + sourceMainWidth + sourceRightWidth;
        if (sourceWidth <= 0f) {
            return;
        }

        float scaleX = destWidth / sourceWidth;
        float x = getX();
        float y = getY();

        if (leftRegion != null) {
            float drawWidth = sourceLeftWidth * scaleX;
            batch.draw(leftRegion, x, y, drawWidth, destHeight);
            x += drawWidth;
        }

        float mainDrawWidth = sourceMainWidth * scaleX;
        batch.draw(mainRegion, x, y, mainDrawWidth, destHeight);
        x += mainDrawWidth;

        if (showRight && rightRegion != null) {
            batch.draw(rightRegion, x, y, sourceRightWidth * scaleX, destHeight);
        }
    }
}
