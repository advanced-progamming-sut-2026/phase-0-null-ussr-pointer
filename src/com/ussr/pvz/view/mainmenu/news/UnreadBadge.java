package com.ussr.pvz.view.mainmenu.news;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;

public class UnreadBadge extends Actor {

    private static final int   TEXTURE_SIZE  = 64;          // pixmap resolution
    private static final float BADGE_SIZE    = 24f;         // scene2d display size
    private static final Color RED_FILL      = new Color(0.92f, 0.13f, 0.13f, 1f);
    private static final Color RING_COLOR    = new Color(1f,  1f,  1f,  0.90f);
    private static final int   RING_PX       = 4;           // white border width in px

    // Shared across all badge instances — created once, never recreated
    private static Texture circleTexture;
    private static Texture ringTexture;

    private final BitmapFont  font;
    private final GlyphLayout layout = new GlyphLayout();
    private final Color       oldColor = new Color();

    private String displayText = "";
    private int    lastCount   = -1;
    private boolean visible_   = false;  // shadow field, drives Actor.setVisible

    public UnreadBadge(Skin skin) {
        setTouchable(Touchable.disabled);
        setSize(BADGE_SIZE, BADGE_SIZE);

        font = skin.has("default", BitmapFont.class)
                ? skin.getFont("default")
                : new BitmapFont();

        ensureTextures();
        setVisible(false);
    }

    // ── Texture creation ────────────────────────────────────────────────────

    private static void ensureTextures() {
        if (circleTexture != null) return;

        circleTexture = buildCircle(TEXTURE_SIZE, RED_FILL,  0);
        ringTexture   = buildCircle(TEXTURE_SIZE, RING_COLOR, 0);
    }

    /**
     * Draws a filled anti-aliased circle into a pixmap.
     * @param size      pixmap side length (square)
     * @param color     fill colour
     * @param inset     shrink circle by this many px on each side
     */
    private static Texture buildCircle(int size, Color color, int inset) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        int cx = size / 2;
        int cy = size / 2;
        float r = (size / 2f) - inset - 0.5f;

        // fill with transparent
        pm.setColor(0, 0, 0, 0);
        pm.fill();

        // draw solid circle pixel-by-pixel for smooth edges
        pm.setColor(color);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist <= r) {
                    // simple anti-alias: blend edge pixel
                    float alpha = Math.min(1f, r - dist + 0.5f);
                    pm.setColor(color.r, color.g, color.b, color.a * alpha);
                    pm.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return tex;
    }

    // ── Update logic ────────────────────────────────────────────────────────

    @Override
    public void act(float delta) {
        super.act(delta);

        Account account = App.getAccount();
        if (account == null) {
            applyVisibility(false, 0);
            return;
        }

        int unread = (int) account.getPersonalNews()
                .stream()
                .filter(n -> !n.isRead())
                .count();

        if (unread == lastCount) return;
        lastCount = unread;

        if (unread <= 0) {
            applyVisibility(false, unread);
        } else {
            displayText = unread > 9 ? "9+" : String.valueOf(unread);
            applyVisibility(true, unread);
        }
    }

    private void applyVisibility(boolean show, int count) {
        visible_ = show;
        setVisible(show);
    }

    // ── Rendering ───────────────────────────────────────────────────────────

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!visible_) return;

        float x = getX();
        float y = getY();
        float s = BADGE_SIZE;

        oldColor.set(batch.getColor());

        // 1. White ring (slightly larger) for contrast against any background
        float ringPad = BADGE_SIZE * (RING_PX / (float) TEXTURE_SIZE) * 2f;
        batch.setColor(RING_COLOR.r, RING_COLOR.g, RING_COLOR.b, parentAlpha);
        batch.draw(ringTexture,
                x - ringPad * 0.5f,
                y - ringPad * 0.5f,
                s + ringPad,
                s + ringPad);

        // 2. Red filled circle
        batch.setColor(RED_FILL.r, RED_FILL.g, RED_FILL.b, parentAlpha);
        batch.draw(circleTexture, x, y, s, s);

        // 3. Count text, centered
        batch.setColor(oldColor);
        float prevScaleX = font.getScaleX();
        float prevScaleY = font.getScaleY();
        font.getData().setScale(0.60f);
        font.setColor(1f, 1f, 1f, parentAlpha);

        layout.setText(font, displayText);
        float textX = x + (s - layout.width)  * 0.5f;
        float textY = y + (s + layout.height) * 0.5f;
        font.draw(batch, displayText, textX, textY);

        font.getData().setScale(prevScaleX, prevScaleY);
        font.setColor(Color.WHITE);
        batch.setColor(oldColor);
    }
}