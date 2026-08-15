package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.dialogue.LevelOutroDialogueRegistry;
import com.ussr.pvz.model.engine.session.GameOutcome;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public final class PostGameDialogueOverlay extends Table {

    private static final float ENTER_DURATION = 1.37f;
    private static final float LEAVE_DURATION = 1.2f;
    private static final String DAVE_PAM      = "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM";
    private static final String SPEECH_BUBBLE = "IMAGE_STORE_SPEECHBUBBLE2";

    private final PamActor daveActor;
    private final Label    dialogueLabel;
    private final Stack    speechBubble;

    private List<String> lines      = List.of();
    private int          currentLine = 0;
    private boolean      entered     = false;
    private boolean      leaving     = false;
    private boolean      triggered   = false;   // so we only react once per outcome

    public PostGameDialogueOverlay(
            Skin skin,
            TextureBank textures,
            PamPlayer pamPlayer
    ) {
        setFillParent(true);
        setTouchable(Touchable.disabled);
        setVisible(false);
        setBackground(skin.newDrawable(
                "white-pixel", new Color(0f, 0f, 0f, 0.52f)));

        // Dave actor — same setup as LevelIntroOverlay
        daveActor = new PamActor(pamPlayer, DAVE_PAM, "anim_enter");
        daveActor.setPamScale(0.82f);
        daveActor.setLooping(false);

        // Speech bubble
        dialogueLabel = new Label("", skin, "big_outline");
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(Align.center);
        dialogueLabel.setColor(new Color(0.22f, 0.12f, 0.04f, 1f));
        speechBubble = buildSpeechBubble(skin, textures);

        // Layout: Dave bottom-left, bubble beside him
        bottom().left();
        pad(0f, 35f, 20f, 20f);
        add(daveActor).width(390f).height(500f).bottom();
        add(speechBubble)
                .width(520f).height(245f)
                .bottom().padBottom(175f).padLeft(-45f);

        // Tap anywhere to advance
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                advance();
            }
        });
    }

    // ── act: watch for game-over and trigger once ────────────────────────────

    @Override
    public void act(float delta) {
        super.act(delta);

        if (triggered) return;

        GameSession session = App.getGameSession();
        if (session == null || !session.isGameOver()) return;
        if (session.getOutcome() == GameOutcome.IN_PROGRESS) return;

        triggered = true;
        String chapterId = session.getLevel() != null
                ? session.getLevel().getChapter() : "";
        String levelId   = session.getLevel() != null
                ? session.getLevel().getId() : "";

        lines = session.isVictory()
                ? LevelOutroDialogueRegistry.getVictoryDialogue(chapterId, levelId)
                : LevelOutroDialogueRegistry.getDefeatDialogue(chapterId, levelId);

        if (lines.isEmpty()) {
            // Nothing to say — immediately unblock the game-over screen
            session.markOutroShown();
            return;
        }

        startEntrance();
    }

    // ── Entrance ─────────────────────────────────────────────────────────────

    private void startEntrance() {
        setVisible(true);
        setTouchable(Touchable.disabled);   // block input until Dave arrives

        speechBubble.setVisible(false);
        daveActor.setClip("anim_enter");
        daveActor.setLooping(false);
        daveActor.resetAnimation();

        addAction(sequence(
                delay(ENTER_DURATION),
                run(() -> {
                    entered = true;
                    setTouchable(Touchable.enabled);
                    speechBubble.setVisible(true);
                    showLine(0);
                })
        ));
    }

    // ── Dialogue advance ─────────────────────────────────────────────────────

    private void advance() {
        if (!entered || leaving) return;
        currentLine++;
        if (currentLine >= lines.size()) {
            startLeave();
        } else {
            showLine(currentLine);
        }
    }

    private void showLine(int index) {
        dialogueLabel.setText(lines.get(index));
        String clip = switch (index % 3) {
            case 0 -> "anim_smalltalk";
            case 1 -> "anim_mediumtalk";
            default -> "anim_blahblah";
        };
        daveActor.setClip(clip);
        daveActor.setLooping(true);
        daveActor.resetAnimation();
    }

    // ── Leave ────────────────────────────────────────────────────────────────

    private void startLeave() {
        leaving = true;
        setTouchable(Touchable.disabled);
        speechBubble.setVisible(false);
        daveActor.setClip("anim_leave");
        daveActor.setLooping(false);
        daveActor.resetAnimation();

        addAction(sequence(
                delay(LEAVE_DURATION),
                run(() -> {
                    GameSession session = App.getGameSession();
                    if (session != null) session.markOutroShown();
                    setVisible(false);
                    remove();
                })
        ));
    }

    // ── Speech bubble (reuse same pattern as LevelIntroOverlay) ─────────────

    private Stack buildSpeechBubble(Skin skin, TextureBank textures) {
        Stack bubble = new Stack();
        TextureRegion region = textures.region(SPEECH_BUBBLE);
        if (region != null) {
            bubble.add(new Image(new TextureRegionDrawable(region)));
        } else {
            Table fallback = new Table();
            fallback.setBackground(
                    skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
            bubble.add(fallback);
        }
        Table textLayer = new Table();
        textLayer.pad(42f, 58f, 50f, 58f);
        textLayer.add(dialogueLabel).width(390f).growY();
        bubble.add(textLayer);
        return bubble;
    }
}