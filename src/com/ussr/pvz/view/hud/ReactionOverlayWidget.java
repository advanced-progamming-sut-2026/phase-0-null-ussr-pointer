package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.shared.multiplayer.ReactionKind;
import com.ussr.pvz.shared.multiplayer.ReactionPayload;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class ReactionOverlayWidget extends Group {

    private static final String[] TEXT_OPTIONS   = { "GG!", "Nice move!", "Good luck!" };
    private static final String[] EMOJI_KEYS = {
            "IMAGE_EFFECTS_PRIZE_PINATA_LUNAR_NEW_YEAR_PRIZE_PINATA_LUNAR_NEW_YEAR_178X226",
            "IMAGE_UI_PACKETS_PUFFBALL",
            "IMAGE_UI_PACKETS_SUNFLOWER"
    };
    private static final String[] STICKER_PAM_PATHS = {
            "768/INITIAL/PLANT/SUNFLOWER/SUNFLOWER.PAM",
            "768/FULL/ZOMBIE/TURKEY/TURKEY.PAM",
            "768/FULL/PLANT/PRIMAL_PEASHOOTER/PRIMAL_PEASHOOTER.PAM"
    };

    private static final float BUBBLE_DISPLAY_SEC = 2.5f;
    private static final float STICKER_SIZE       = 200f;

    private final Skin        skin;
    private final TextureBank textures;
    private final PamPlayer   pamPlayer;

    public ReactionOverlayWidget(Skin skin, TextureBank textures, PamPlayer pamPlayer) {
        this.skin      = skin;
        this.textures  = textures;
        this.pamPlayer = pamPlayer;
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    public void onReactionReceived(GameEvent.ReactionReceivedEvent event) {
        show(event.reaction());
    }

    private void show(ReactionPayload payload) {
        clearChildren();
        clearActions();

        switch (payload.kind()) {
            case TEXT    -> showTextBubble(payload);
            case EMOJI   -> showEmojiBubble(payload);
            case STICKER -> showSticker(payload);
        }
    }

    // ── Bubbles ───────────────────────────────────────────────────────────────

    private void showTextBubble(ReactionPayload payload) {
        String content = safeGet(TEXT_OPTIONS, payload.index());

        Label bubble = new Label(content, skin, "default");
        bubble.setAlignment(Align.center);
        bubble.setFontScale(1.4f);
        bubble.setColor(Color.WHITE);
        bubble.pack();

        animateBubble(bubble);
    }

    private void showEmojiBubble(ReactionPayload payload) {
        String key = safeGet(EMOJI_KEYS, payload.index());
        TextureRegion region = textures.region(key);

        Actor bubble;
        if (region != null) {
            bubble = new Image(new TextureRegionDrawable(region));
            bubble.setSize(96f, 96f);
        } else {
            Label lbl = new Label("EMOJI", skin, "default");
            lbl.setFontScale(2.2f);
            lbl.pack();
            bubble = lbl;
        }

        animateBubble(bubble);
    }

    private void animateBubble(Actor bubble) {
        float stageW = getWidth();
        float startY = getHeight() + bubble.getHeight();
        float endY   = getHeight() * 0.78f;

        bubble.setPosition((stageW - bubble.getWidth()) / 2f, startY);
        addActor(bubble);

        bubble.addAction(Actions.sequence(
                Actions.moveTo(bubble.getX(), endY, 0.4f, Interpolation.swingOut),
                Actions.delay(BUBBLE_DISPLAY_SEC),
                Actions.fadeOut(0.5f),
                Actions.removeActor()
        ));
    }

    // ── Sticker (Using PamActor) ──────────────────────────────────────────────

    private void showSticker(ReactionPayload payload) {
        String pamPath = safeGet(STICKER_PAM_PATHS, payload.index());

        // Spawn a full PamActor using the native animation engine
        PamActor stickerActor = new PamActor(pamPlayer, pamPath, "idle");
        stickerActor.setPamScale(1.3f); // Scale up so it's clearly visible as a reaction
        stickerActor.setSize(STICKER_SIZE, STICKER_SIZE);

        float endX   = getWidth() * 0.6f;
        float startX = getWidth() + stickerActor.getWidth();
        float midY   = (getHeight() - stickerActor.getHeight()) / 2f;

        stickerActor.setPosition(startX, midY);
        addActor(stickerActor);

        stickerActor.addAction(Actions.sequence(
                Actions.moveTo(endX, midY, 0.55f, Interpolation.elasticOut),
                Actions.delay(2.2f),
                Actions.parallel(
                        Actions.moveTo(startX, midY, 0.35f, Interpolation.fastSlow),
                        Actions.fadeOut(0.3f)
                ),
                Actions.removeActor()
        ));
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private static String safeGet(String[] arr, int index) {
        return (index >= 0 && index < arr.length) ? arr[index] : arr[0];
    }
}