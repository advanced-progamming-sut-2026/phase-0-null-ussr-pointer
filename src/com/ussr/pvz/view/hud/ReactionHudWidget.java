package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.behavior.MultiplayerIZombieBehavior;
import com.ussr.pvz.service.minigame.MultiplayerIZombieService;
import com.ussr.pvz.shared.multiplayer.ReactionKind;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class ReactionHudWidget extends Table {

    // ── Predefined reaction content ───────────────────────────────────────────

    private static final String[] TEXT_OPTIONS  = { "GG!", "Nice move!", "Good luck!" };

    private static final String[] EMOJI_KEYS = {
            "IMAGE_EFFECTS_PRIZE_PINATA_LUNAR_NEW_YEAR_PRIZE_PINATA_LUNAR_NEW_YEAR_178X226",
            "IMAGE_UI_PACKETS_PUFFBALL",
            "IMAGE_UI_PACKETS_SUNFLOWER"
    };
    private static final String[] EMOJI_LABELS = { "Prize", "Puffball", "Sunflower" };

    // Now using PAM paths instead of texture keys!
    private static final String[] STICKER_PAM_PATHS  = {
            "768/INITIAL/PLANT/SUNFLOWER/SUNFLOWER.PAM",
            "768/FULL/ZOMBIE/TURKEY/TURKEY.PAM",
            "768/FULL/PLANT/PRIMAL_PEASHOOTER/PRIMAL_PEASHOOTER.PAM"
    };

    // ── State ─────────────────────────────────────────────────────────────────

    private final Skin        skin;
    private final TextureBank textures;
    private final PamPlayer   pamPlayer;

    private boolean panelOpen = false;

    private final Table optionRow = new Table();

    public ReactionHudWidget(Skin skin, TextureBank textures, PamPlayer pamPlayer) {
        this.skin      = skin;
        this.textures  = textures;
        this.pamPlayer = pamPlayer;

        setTouchable(Touchable.childrenOnly);
        setVisible(false);

        // ── Toggle & Tab buttons using the provided atlas keys ────────────────
        Button toggleBtn = createAtlasButton("IMAGE_NOTES_ZOMBIENOTE_EGYPT", "💬");
        toggleBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                setPanelOpen(!panelOpen);
            }
        });

        Table tabRow = new Table();
        tabRow.setVisible(false);

        Button tabText  = createAtlasButton("IMAGE_NOTES_ZNOTE_FUTUREBG", "💬");
        Button tabEmoji = createAtlasButton("IMAGE_ZOMBIE_ZOMBIE_FUTURE_ZOMBOSS_ZOMBIE_FUTURE_ZOMBOSS_229X117",
                "😀");
        Button tabStick = createAtlasButton("IMAGE_ZOMBIE_ZOMBIE_ROMAN_ZOMBOSS_ZOMBIE_ROMAN_ZOMBOSS_108X141_10",
                "🎭");

        tabText.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { switchTab(ReactionKind.TEXT, tabRow); }
        });
        tabEmoji.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { switchTab(ReactionKind.EMOJI, tabRow); }
        });
        tabStick.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { switchTab(ReactionKind.STICKER, tabRow); }
        });

        tabRow.add(tabText).size(40f, 40f).pad(2f);
        tabRow.add(tabEmoji).size(40f, 40f).pad(2f);
        tabRow.add(tabStick).size(40f, 40f).pad(2f);

        // ── Layout ────────────────────────────────────────────────────────────
        add(toggleBtn).size(48f, 48f).row();
        add(tabRow).row();
        add(optionRow);

        rebuildOptions(ReactionKind.TEXT, tabRow);
    }

    private Button createAtlasButton(String key, String fallbackText) {
        TextureRegion region = textures.region(key);
        if (region != null) {
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.imageUp = new TextureRegionDrawable(region);
            return new ImageButton(style);
        }
        return new TextButton(fallbackText, skin);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        boolean inMultiplayer = session != null
                && session.getLevel() != null
                && session.getLevel().getBehavior()
                instanceof MultiplayerIZombieBehavior;

        if (!inMultiplayer && isVisible()) {
            setPanelOpen(false);
            setVisible(false);
        } else if (inMultiplayer && !isVisible()) {
            setVisible(true);
        }
    }

    private void setPanelOpen(boolean open) {
        panelOpen = open;
        getChildren().get(1).setVisible(open);
        optionRow.setVisible(open);
    }

    private void switchTab(ReactionKind kind, Table tabRow) {
        rebuildOptions(kind, tabRow);
    }

    private void rebuildOptions(ReactionKind kind, Table tabRow) {
        optionRow.clearChildren();
        optionRow.setVisible(panelOpen);

        switch (kind) {
            case TEXT -> {
                for (int i = 0; i < TEXT_OPTIONS.length; i++) {
                    final int idx = i;
                    TextButton btn = new TextButton(TEXT_OPTIONS[i], skin);
                    btn.addListener(new ClickListener() {
                        @Override public void clicked(InputEvent e, float x, float y) { sendAndClose(ReactionKind.TEXT,
                                idx); }
                    });
                    optionRow.add(btn).pad(3f).minWidth(80f);
                }
            }
            case EMOJI -> {
                for (int i = 0; i < EMOJI_KEYS.length; i++) {
                    final int idx = i;
                    Button btn = createAtlasButton(EMOJI_KEYS[i], EMOJI_LABELS[i]);
                    btn.addListener(new ClickListener() {
                        @Override public void clicked(InputEvent e, float x, float y) { sendAndClose(ReactionKind.EMOJI,
                                idx); }
                    });
                    optionRow.add(btn).size(56f, 56f).pad(3f);
                }
            }
            case STICKER -> {
                for (int i = 0; i < STICKER_PAM_PATHS.length; i++) {
                    final int idx = i;
                    // Render the PAM directly into the HUD menu as a clickable actor!
                    PamActor pamBtn = new PamActor(pamPlayer, STICKER_PAM_PATHS[i], "idle");
                    pamBtn.setPamScale(0.45f); // Scale down for the UI menu
                    pamBtn.setSize(72f, 72f);
                    pamBtn.setTouchable(Touchable.enabled);
                    pamBtn.addListener(new ClickListener() {
                        @Override public void clicked(InputEvent e, float x, float y) { sendAndClose(
                                ReactionKind.STICKER, idx); }
                    });
                    optionRow.add(pamBtn).size(72f, 72f).pad(3f);
                }
            }
        }
    }

    private void sendAndClose(ReactionKind kind, int index) {
        MultiplayerIZombieService.getInstance().sendReaction(kind, index);
        setPanelOpen(false);
    }
}