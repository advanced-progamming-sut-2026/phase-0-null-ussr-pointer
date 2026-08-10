package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.dialogue.LevelDialogueRegistry;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/** A blocking Crazy Dave conversation that appears shortly after level start. */
public final class LevelIntroOverlay extends Table {
    private static final float APPEAR_DELAY = 2f;
    private static final float ENTER_DURATION = 1.37f;
    private static final float LEAVE_DURATION = 1.2f;
    private static final String DAVE_PAM =
            "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM";
    private static final String SPEECH_BUBBLE =
            "IMAGE_STORE_SPEECHBUBBLE2";

    private final GameplayController controller;
    private final GameSession session;
    private final List<String> dialogue;
    private final PamActor daveActor;
    private final Label dialogueLabel;
    private final Stack speechBubble;

    private int currentLine;
    private boolean dialogueOpen;
    private boolean leaving;

    public LevelIntroOverlay(
            Skin skin,
            TextureBank textures,
            PamPlayer pamPlayer,
            GameplayController controller,
            GameSession session
    ) {
        this.controller = controller;
        this.session = session;
        this.dialogue = resolveDialogue(session);

        setFillParent(true);
        setTouchable(Touchable.disabled);
        setVisible(false);
        setBackground(skin.newDrawable(
                "white-pixel",
                new Color(0f, 0f, 0f, 0.48f)
        ));

        daveActor = new PamActor(pamPlayer, DAVE_PAM, "anim_enter");
        daveActor.setPamScale(0.82f);
        daveActor.setLooping(false);

        dialogueLabel = new Label("", skin, "big_outline");
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        dialogueLabel.setColor(new Color(0.22f, 0.12f, 0.04f, 1f));

        speechBubble = createSpeechBubble(skin, textures);
        buildLayout();

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                advanceDialogue();
            }
        });

        if (session == null || session.isLevelIntroShown()) {
            remove();
        } else {
            addAction(sequence(delay(APPEAR_DELAY), run(this::openDialogue)));
        }
    }

    private List<String> resolveDialogue(GameSession gameSession) {
        if (gameSession == null || gameSession.getLevel() == null) {
            return List.of();
        }
        return LevelDialogueRegistry.getDialogue(
                gameSession.getLevel().getChapter(),
                gameSession.getLevel().getId()
        );
    }

    private Stack createSpeechBubble(Skin skin, TextureBank textures) {
        Stack speech = new Stack();
        TextureRegion region = textures.region(SPEECH_BUBBLE);

        if (region != null) {
            speech.add(new Image(new TextureRegionDrawable(region)));
        } else {
            Table fallback = new Table();
            fallback.setBackground(skin.getDrawable(
                    "image_ui_dialog_asset_inner_bkgd_10"
            ));
            speech.add(fallback);
        }

        Table textLayer = new Table();
        textLayer.pad(42f, 58f, 50f, 58f);
        textLayer.add(dialogueLabel).width(390f).growY();
        speech.add(textLayer);
        return speech;
    }

    private void buildLayout() {
        bottom().left();
        pad(0f, 35f, 20f, 20f);
        add(daveActor).width(390f).height(500f).bottom();
        add(speechBubble)
                .width(520f)
                .height(245f)
                .bottom()
                .padBottom(175f)
                .padLeft(-45f);
    }

    private void openDialogue() {
        if (session == null || session.isGameOver() || dialogue.isEmpty()) {
            remove();
            return;
        }

        session.markLevelIntroShown();
        controller.setDialoguePaused(true);
        currentLine = 0;
        leaving = false;
        dialogueOpen = false;

        setVisible(true);
        setTouchable(Touchable.enabled);
        speechBubble.setVisible(false);
        daveActor.setClip("anim_enter");
        daveActor.setLooping(false);
        daveActor.resetAnimation();

        addAction(sequence(
                delay(ENTER_DURATION),
                run(() -> {
                    dialogueOpen = true;
                    speechBubble.setVisible(true);
                    showCurrentLine();
                })
        ));
    }

    private void advanceDialogue() {
        if (!dialogueOpen || leaving) {
            return;
        }

        currentLine++;
        if (currentLine >= dialogue.size()) {
            finishDialogue();
        } else {
            showCurrentLine();
        }
    }

    private void showCurrentLine() {
        dialogueLabel.setText(dialogue.get(currentLine));
        String clip = switch (currentLine % 3) {
            case 0 -> "anim_smalltalk";
            case 1 -> "anim_mediumtalk";
            default -> "anim_blahblah";
        };
        daveActor.setClip(clip);
        daveActor.setLooping(true);
        daveActor.resetAnimation();
    }

    private void finishDialogue() {
        leaving = true;
        dialogueOpen = false;
        speechBubble.setVisible(false);
        daveActor.setClip("anim_leave");
        daveActor.setLooping(false);
        daveActor.resetAnimation();

        addAction(sequence(
                delay(LEAVE_DURATION),
                run(() -> {
                    controller.setDialoguePaused(false);
                    setTouchable(Touchable.disabled);
                    remove();
                })
        ));
    }

    @Override
    public boolean remove() {
        if (controller != null && dialogueOpen) {
            controller.setDialoguePaused(false);
        }
        return super.remove();
    }
}
