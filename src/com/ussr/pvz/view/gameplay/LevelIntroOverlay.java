package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.dialogue.LevelDialogueRegistry;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.service.minigame.MultiplayerIZombieService;
import com.ussr.pvz.view.animation.PamActor;
import com.ussr.pvz.view.hud.ObjectiveWidgetFactory;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * A blocking overlay that appears shortly after level start.
 *
 * <p>Flow:
 * <ol>
 *   <li>Crazy Dave walks in and delivers his dialogue lines (if any).
 *   <li>After the last line, if this level has text objectives
 *       (TimedWar, Endless, AllowedPlantsLost, SaveOurSeeds …) an
 *       "Objective card" is shown — still paused, same backdrop.
 *   <li>The player taps the card to dismiss it, Dave walks out, and
 *       the game unpauses.
 * </ol>
 */
public final class LevelIntroOverlay extends Table {

    private static final float APPEAR_DELAY   = 2f;
    private static final float ENTER_DURATION = 1.37f;
    private static final float LEAVE_DURATION = 1.2f;

    private static final String DAVE_PAM      = "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM";
    private static final String SPEECH_BUBBLE = "IMAGE_STORE_SPEECHBUBBLE2";

    // ── model / controller ───────────────────────────────────────────────────
    private final GameplayController controller;
    private final GameSession         session;
    private final List<String>        dialogue;
    private final List<String>        objectives;

    // ── Dave / speech-bubble sub-widgets ─────────────────────────────────────
    private final PamActor  daveActor;
    private final Label     dialogueLabel;
    private final Stack     speechBubble;

    // ── Objective card ────────────────────────────────────────────────────────
    private final Table     objectiveCard;
    private final Label     objectiveLabel;

    // ── state ─────────────────────────────────────────────────────────────────
    private int     currentLine;
    private boolean dialogueOpen;
    private boolean leaving;
    private boolean showingObjective;
    private boolean completionNotified;

    // =========================================================================
    // Constructor
    // =========================================================================
    public LevelIntroOverlay(
            Skin         skin,
            TextureBank  textures,
            PamPlayer    pamPlayer,
            GameplayController controller,
            GameSession  session
    ) {
        this.controller = controller;
        this.session    = session;
        this.dialogue   = resolveDialogue(session);
        this.objectives = ObjectiveWidgetFactory.collectTextObjectives(session);

        setFillParent(true);
        setTouchable(Touchable.disabled);
        setVisible(false);
        setBackground(skin.newDrawable("white-pixel", new Color(0f, 0f, 0f, 0.48f)));

        // ── Dave actor ────────────────────────────────────────────────────────
        daveActor = new PamActor(pamPlayer, DAVE_PAM, "anim_enter");
        daveActor.setPamScale(0.82f);
        daveActor.setLooping(false);

        // ── Speech bubble ─────────────────────────────────────────────────────
        dialogueLabel = new Label("", skin, "big_outline");
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        dialogueLabel.setColor(Color.WHITE);
        speechBubble = createSpeechBubble(skin, textures);

        // ── Objective card ────────────────────────────────────────────────────
        objectiveLabel = new Label("", skin, "medium_outline");
        objectiveLabel.setWrap(true);
        objectiveLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        objectiveCard  = buildObjectiveCard(skin, textures);

        buildLayout();

        // ── Click to advance / dismiss ────────────────────────────────────────
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (showingObjective) {
                    dismissObjectiveCard();
                } else {
                    advanceDialogue();
                }
            }
        });

        // ── Boot ──────────────────────────────────────────────────────────────
        if (session == null || session.isLevelIntroShown()) {
            // No intro at all — but we still might need to show objective card.
            // Show it immediately (briefly paused) if there are text objectives.
            if (!objectives.isEmpty()) {
                addAction(sequence(delay(0.5f), run(this::openObjectiveCardDirectly)));
            } else {
                completeIntro();
            }
        } else {
            if (dialogue.isEmpty() && objectives.isEmpty()) {
                completeIntro();
            } else {
                addAction(sequence(delay(APPEAR_DELAY), run(this::openDialogue)));
            }
        }
    }

    // =========================================================================
    // Layout helpers
    // =========================================================================
    private List<String> resolveDialogue(GameSession gs) {
        if (gs == null || gs.getLevel() == null) return List.of();
        return LevelDialogueRegistry.getDialogue(
                gs.getLevel().getChapter(),
                gs.getLevel().getId()
        );
    }

    private Stack createSpeechBubble(Skin skin, TextureBank textures) {
        Stack speech = new Stack();
        TextureRegion region = textures.region(SPEECH_BUBBLE);
        if (region != null) {
            speech.add(new Image(new TextureRegionDrawable(region)));
        } else {
            Table fallback = new Table();
            fallback.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
            speech.add(fallback);
        }
        Table textLayer = new Table();
        textLayer.pad(42f, 58f, 50f, 58f);
        textLayer.add(dialogueLabel).width(390f).growY();
        speech.add(textLayer);
        return speech;
    }

    /**
     * Builds the semi-opaque card that displays the level objectives.
     * It is hidden by default; {@link #showObjectiveCard()} makes it visible.
     */
    private Table buildObjectiveCard(Skin skin, TextureBank textures) {
        Table card = new Table();

        // Try to use the speech-bubble texture as a background panel, else
        // fall back to a plain dark rounded rect using the dialog asset.
        TextureRegion bubble = textures.region(SPEECH_BUBBLE);
        if (bubble != null) {
            // Reuse the same speech-bubble image as a backdrop so it matches
            // the Crazy Dave dialog aesthetic.
            Stack stack = new Stack();
            stack.add(new Image(new TextureRegionDrawable(bubble)));
            Table content = new Table();
            content.pad(48f, 60f, 56f, 60f);
            content.add(objectiveLabel).width(420f).growY().row();

            Label tapHint = new Label("Tap to continue", skin, "medium_outline");
            tapHint.setAlignment(com.badlogic.gdx.utils.Align.center);
            tapHint.setColor(new Color(1f, 1f, 1f, 0.8f));
            content.add(tapHint).padTop(12f);
            stack.add(content);
            card.add(stack).width(560f).height(280f);
        } else {
            card.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
            card.pad(48f, 60f, 56f, 60f);
            card.add(objectiveLabel).width(420f).growY().row();
            Label tapHint = new Label("Tap to continue", skin, "medium_outline");
            tapHint.setAlignment(com.badlogic.gdx.utils.Align.center);
            tapHint.setColor(new Color(1f, 1f, 1f, 0.8f));
            card.add(tapHint).padTop(12f);
        }

        card.setVisible(false);
        return card;
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

        // Objective card floats in the center of the screen.
        // We add it to a full-screen overlay table layered over everything.
        Table centerLayer = new Table();
        centerLayer.setFillParent(true);
        centerLayer.center();
        centerLayer.add(objectiveCard);
        addActor(centerLayer);   // addActor so it sits above without breaking Table layout
    }

    // =========================================================================
    // Dialogue flow
    // =========================================================================
    private void openDialogue() {
        if (session == null || session.isGameOver()) { remove(); return; }

        if (dialogue.isEmpty()) {
            // No Dave lines → jump straight to objective card (or close).
            if (!objectives.isEmpty()) {
                markIntroShown();
                controller.setDialoguePaused(true);
                setVisible(true);
                setTouchable(Touchable.enabled);
                speechBubble.setVisible(false);
                daveActor.setClip("anim_enter");
                daveActor.setLooping(false);
                daveActor.resetAnimation();
                addAction(sequence(delay(ENTER_DURATION), run(this::showObjectiveCard)));
            } else {
                remove();
            }
            return;
        }

        markIntroShown();
        controller.setDialoguePaused(true);
        currentLine   = 0;
        leaving       = false;
        dialogueOpen  = false;

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
        if (!dialogueOpen || leaving) return;

        currentLine++;
        if (currentLine >= dialogue.size()) {
            // All Dave lines done — show objective card or leave.
            dialogueOpen = false;
            speechBubble.setVisible(false);
            daveActor.setClip("anim_smalltalk");
            daveActor.setLooping(true);
            daveActor.resetAnimation();

            if (!objectives.isEmpty()) {
                showObjectiveCard();
            } else {
                startLeaving();
            }
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

    // =========================================================================
    // Objective card flow
    // =========================================================================

    /**
     * Called when the intro was already shown (no dialogue) but objectives
     * still need to be communicated on first-ever run.
     */
    private void openObjectiveCardDirectly() {
        if (session == null || session.isGameOver()) { remove(); return; }
        controller.setDialoguePaused(true);
        setVisible(true);
        setTouchable(Touchable.enabled);
        speechBubble.setVisible(false);
        daveActor.setClip("anim_smalltalk");
        daveActor.setLooping(true);
        daveActor.resetAnimation();
        showObjectiveCard();
    }

    private void showObjectiveCard() {
        showingObjective = true;
        objectiveLabel.setText(String.join("\n\n", objectives));
        objectiveCard.setVisible(true);
        // Dave idles while the player reads
        daveActor.setClip("anim_smalltalk");
        daveActor.setLooping(true);
        daveActor.resetAnimation();
    }

    private void dismissObjectiveCard() {
        showingObjective = false;
        objectiveCard.setVisible(false);
        startLeaving();
    }

    // =========================================================================
    // Dave leave flow
    // =========================================================================
    private void startLeaving() {
        leaving = true;
        speechBubble.setVisible(false);
        daveActor.setClip("anim_leave");
        daveActor.setLooping(false);
        daveActor.resetAnimation();

        addAction(sequence(
                delay(LEAVE_DURATION),
                run(() -> {
                    completeIntro();
                })
        ));
    }

    private void completeIntro() {
        if (completionNotified) {
            return;
        }

        completionNotified = true;
        controller.setDialoguePaused(false);
        markIntroShown();
        MultiplayerIZombieService.getInstance()
                .markLocalPlayerReady();
        setTouchable(Touchable.disabled);
        setVisible(false);
        remove();
    }

    private void markIntroShown() {
        if (session != null) session.markLevelIntroShown();
    }

    // =========================================================================
    // Cleanup
    // =========================================================================
    @Override
    public boolean remove() {
        if (controller != null && (dialogueOpen || showingObjective)) {
            controller.setDialoguePaused(false);
        }
        return super.remove();
    }
}