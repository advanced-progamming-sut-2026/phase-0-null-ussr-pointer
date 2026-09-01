package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.IZombiePricing;
import com.ussr.pvz.model.level.behavior.CouchIZombieBehavior;
import com.ussr.pvz.model.level.behavior.IZombieBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.level.behavior.MultiplayerIZombieBehavior;
import pvz.libpvz.textures.TextureBank;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Horizontal zombie-selection row shown at the top-right of the screen
 * during i,Zombie minigame levels, mirroring {@link SeedBankHud} on the
 * left. In couch play both panels are visible at once so each player sees
 * only their own side's options.
 *
 * <p>Each card shows the zombie's portrait texture (or a coloured fallback),
 * its sun cost, and a cooldown-style greyed state when the player can't afford
 * it.  Clicking selects the type; clicking the lawn (via
 * {@link GameplayController#handleGridClick}) places it.</p>
 */
public class IZombieHud extends Table {

    private static final int   SLOT_W           = 100;
    private static final int   SLOT_H           = 95;
    private static final float PLACE_COOLDOWN_S = 5f;   // hard-coded per-zombie cooldown

    private final Skin               skin;
    private final TextureBank        textures;
    private final GameplayController controller;

    private final Label sunLabel;
    private final Table cardColumn;
    private final Map<String, ZombieSlotWidget> slots = new LinkedHashMap<>();

    private String      selectedZombieKey = null;
    private GameSession lastSession       = null;

    public IZombieHud(Skin skin, TextureBank textures, GameplayController controller) {
        this.skin       = skin;
        this.textures   = textures;
        this.controller = controller;
        top().right();
        setTouchable(Touchable.childrenOnly);
        setVisible(false);

        sunLabel = new Label("0", skin, "default");

        Table sunCounter = buildCounter();

        cardColumn = new Table();
        cardColumn.top().right();

        add(sunCounter).width(100f).height(38f).padBottom(6f).right().row();
        add(cardColumn).top().right().row();
    }

    private Table buildCounter() {
        Table counter = new Table();
        TextureRegion bg = textures.region("IMAGE_UI_HUD_INGAME_BACKGROUND_3SLICE");
        if (bg != null) counter.setBackground(new TextureRegionDrawable(bg));
        TextureRegion icon = textures.region("IMAGE_UI_ALMANAC_STAT_ICON_SUNCOST_LAYER_1");
        if (icon != null) counter.add(new Image(icon)).size(24f).padLeft(6f).padRight(4f);
        counter.add(sunLabel).minWidth(40f).padRight(6f);
        return counter;
    }

    // ── act ──────────────────────────────────────────────────────────────────

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (!shouldShowFor(session)) {
            if (isVisible()) clearSelection();
            setVisible(false);
            setTouchable(Touchable.disabled);
            lastSession = null;
            return;
        }

        setVisible(true);
        setTouchable(Touchable.childrenOnly);

        if (session != lastSession) {
            rebuildCards(session);
            lastSession = session;
        }

        int sun = getZombieSunCount(session);
        sunLabel.setText(String.valueOf(sun));

        // Tick cooldowns and refresh affordability every frame
        for (ZombieSlotWidget slot : slots.values()) {
            slot.tick(delta);
            slot.refreshAffordability(sun);
        }
    }

    private int getZombieSunCount(GameSession session) {
        LevelBehavior behavior = session.getLevel().getBehavior();
        if (behavior instanceof CouchIZombieBehavior couchBehavior) {
            return couchBehavior.getZombieSun();
        }
        return session.getSunCount();
    }

    private boolean shouldShowFor(GameSession session) {
        if (session == null || session.getLevel() == null) return false;
        LevelBehavior behavior = session.getLevel().getBehavior();
        if (behavior instanceof IZombieBehavior || behavior instanceof CouchIZombieBehavior) return true;
        return behavior instanceof MultiplayerIZombieBehavior multiplayer
                && multiplayer.isZombiesPlayer();
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    private void rebuildCards(GameSession session) {
        cardColumn.clearChildren();
        slots.clear();
        selectedZombieKey = null;
        controller.setSelectedZombieKey(null);

        for (var entry : session.getLevel().getAllowedZombies()) {
            String id = entry.id();
            if ("SunProducerZombie".equalsIgnoreCase(id)) continue;

            int cost = IZombiePricing.getCost(session, id);
            ZombieSlotWidget slot = new ZombieSlotWidget(
                    id, cost, skin, textures,
                    () -> selectZombie(id)
            );
            slots.put(id, slot);
            cardColumn.add(slot).size(SLOT_W, SLOT_H).padLeft(3f).top();
        }
    }

    private void selectZombie(String key) {
        if (key.equals(selectedZombieKey)) {
            selectedZombieKey = null;
        } else {
            selectedZombieKey = key;
        }
        controller.setSelectedZombieKey(selectedZombieKey);
        for (Map.Entry<String, ZombieSlotWidget> e : slots.entrySet()) {
            e.getValue().setSelected(e.getKey().equals(selectedZombieKey));
        }
    }

    /**
     * Called by the controller after a zombie has been successfully placed.
     * Deselects the current slot and starts its cooldown.
     */
    public void onZombiePlaced(String zombieKey) {
        if (zombieKey != null && slots.containsKey(zombieKey)) {
            slots.get(zombieKey).startCooldown(PLACE_COOLDOWN_S);
        }
        clearSelection();
    }

    public void clearSelection() {
        selectedZombieKey = null;
        controller.setSelectedZombieKey(null);
        for (ZombieSlotWidget slot : slots.values()) slot.setSelected(false);
    }

    // =========================================================================
    // Inner widget: one zombie slot card
    // =========================================================================
    private static class ZombieSlotWidget extends Stack {

        private static final Color AFFORDABLE   = Color.WHITE;
        private static final Color UNAFFORDABLE = new Color(0.5f, 0.5f, 0.5f, 1f);
        private static final Color BORDER_COLOR = new Color(1f, 0.72f, 0.08f, 1f);

        private final int             cost;
        private final Image           portrait;
        private final Label           costLabel;
        private final Actor           selectionFrame;
        private final CooldownOverlay cooldownOverlay;

        private boolean affordable = true;

        // cooldown state
        private float cooldownTotal     = 0f;
        private float cooldownRemaining = 0f;

        ZombieSlotWidget(String zombieId, int cost, Skin skin, TextureBank textures, Runnable onClick) {
            this.cost     = cost;
            setTouchable(Touchable.enabled);
            if (skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                add(new Image(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10")));
            }
            String texKey = com.ussr.pvz.model.entities.zombies.ZombieFactory.getZombieTextureRegion(zombieId);
            TextureRegion portRegion = textures.region(texKey);
            portrait = portRegion != null ? new Image(portRegion) : new Image();
            portrait.setScaling(Scaling.fit);
            portrait.setTouchable(Touchable.disabled);
            Table portraitLayer = new Table();
            portraitLayer.setTouchable(Touchable.disabled);
            portraitLayer.add(portrait).grow().pad(4f);
            add(portraitLayer);
            if (portRegion == null) {
                Table nameLayer = new Table();
                nameLayer.setTouchable(Touchable.disabled);
                Label nameLabel = new Label(shortName(zombieId), skin, "default");
                nameLabel.setFontScale(0.42f);
                nameLabel.setAlignment(Align.center);
                nameLabel.setWrap(true);
                nameLayer.add(nameLabel).growX().center().pad(4f);
                add(nameLayer);
            }
            Table costLayer = new Table();
            costLayer.setTouchable(Touchable.disabled);
            costLayer.bottom().left();
            costLabel = new Label(String.valueOf(cost), skin, "default");
            costLabel.setFontScale(0.62f);
            costLabel.setAlignment(Align.left);
            costLayer.add(costLabel).pad(2f);
            add(costLayer);
            cooldownOverlay = new CooldownOverlay();
            add(cooldownOverlay);
            Drawable goldPixel = skin.has("white-pixel", Drawable.class) ? skin.newDrawable("white-pixel",
                    BORDER_COLOR) : null;
            selectionFrame = new BorderFrameActor(goldPixel);
            selectionFrame.setTouchable(Touchable.disabled);
            selectionFrame.setVisible(false);
            add(selectionFrame);
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isOnCooldown()) return;
                    onClick.run();
                }
            });
        }

        // ── Cooldown ──────────────────────────────────────────────────────────

        void startCooldown(float seconds) {
            cooldownTotal     = seconds;
            cooldownRemaining = seconds;
            cooldownOverlay.setProgress(1f);
        }

        boolean isOnCooldown() {
            return cooldownRemaining > 0f;
        }

        void tick(float delta) {
            if (cooldownRemaining <= 0f) return;
            cooldownRemaining = Math.max(0f, cooldownRemaining - delta);
            // progress goes from 1 → 0 as cooldown drains (curtain retracts downward)
            float progress = cooldownTotal > 0f ? cooldownRemaining / cooldownTotal : 0f;
            cooldownOverlay.setProgress(progress);
        }

        // ── Affordability ─────────────────────────────────────────────────────

        void refreshAffordability(int sun) {
            affordable = sun >= cost && !isOnCooldown();
            Color tint = affordable ? AFFORDABLE : UNAFFORDABLE;
            portrait.setColor(tint);
            costLabel.setColor(affordable ? Color.WHITE : new Color(0.85f, 0.35f, 0.3f, 1f));
        }

        // ── Selection ─────────────────────────────────────────────────────────

        void setSelected(boolean sel) {
            setScale(sel ? 1.05f : 1f);
            setOrigin(Align.center);
            selectionFrame.setVisible(sel);
        }

        private static String shortName(String id) {
            return id.startsWith("Zombie") ? id.substring(6) : id;
        }
    }

    // ── Border frame ──────────────────────────────────────────────────────────
    private static final class BorderFrameActor extends Actor {
        private static final float BW = 5f;
        private final Drawable pixel;
        BorderFrameActor(Drawable pixel) { this.pixel = pixel; }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (pixel == null) return;
            float x = getX(), y = getY(), w = getWidth(), h = getHeight();
            pixel.draw(batch, x, y, w, BW);
            pixel.draw(batch, x, y + h - BW, w, BW);
            pixel.draw(batch, x, y, BW, h);
            pixel.draw(batch, x + w - BW, y, BW, h);
        }
    }
}