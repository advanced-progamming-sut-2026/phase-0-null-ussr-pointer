package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.LobbyService;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class GlobalInviteOverlay extends Table {

    // ── Listener — called when an invite result is processed ──────────────────

    /**
     * Implement this in LobbyMenu and pass it via setInviteListener().
     * The overlay calls it whenever it resolves an outgoing-invite result
     * so the lobby UI can reset itself without polling on its own.
     */
    public interface InviteListener {
        /** Called when the invite WE sent was accepted. */
        void onInviteAccepted(String opponentUsername);
        /** Called when the invite WE sent was rejected. */
        void onInviteRejected(String opponentUsername);
    }

    // ── Polling ───────────────────────────────────────────────────────────────

    private static final float POLL_INTERVAL = 2.5f;
    private float pollTimer = 0f;

    // ── Internal state ────────────────────────────────────────────────────────

    private enum OverlayMode {
        HIDDEN,
        INCOMING_INVITE,
        INVITE_ACCEPTED,
        INVITE_REJECTED,
        MATCH_FOUND
    }

    private OverlayMode mode = OverlayMode.HIDDEN;
    private String pendingInviteTarget;
    private boolean inRandomQueue = false;

    /** Registered by LobbyMenu; null when lobby is not on screen */
    private InviteListener inviteListener;

    // ── Services ──────────────────────────────────────────────────────────────

    private final LobbyService lobbyService = new LobbyService();

    // ── Widgets ───────────────────────────────────────────────────────────────

    private final Table card;
    private final Label titleLabel;
    private final Label bodyLabel;
    private final TextButton acceptButton;
    private final TextButton rejectButton;
    private final TextButton okButton;

    // ── Constructor ───────────────────────────────────────────────────────────

    public GlobalInviteOverlay(Skin skin) {
        setFillParent(true);
        setTouchable(Touchable.disabled);

        card = new Table();
        card.setBackground(skin.newDrawable(
                "white-pixel",
                new Color(0.08f, 0.12f, 0.08f, 0.96f)
        ));
        card.pad(18f, 22f, 18f, 22f);

        titleLabel = new Label("", skin, "medium_outline");
        titleLabel.setAlignment(Align.center);

        bodyLabel = new Label("", skin, "default");
        bodyLabel.setWrap(true);
        bodyLabel.setAlignment(Align.center);

        acceptButton = new TextButton("", skin, "default");
        rejectButton = new TextButton("", skin, "default");
        okButton     = new TextButton("OK", skin, "default");

        acceptButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { onAccept(); }
        });
        rejectButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { onReject(); }
        });
        okButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { onOk(); }
        });

        card.add(titleLabel).fillX().padBottom(8f).row();
        card.add(bodyLabel).width(280f).padBottom(14f).row();

        Table buttons = new Table();
        buttons.add(acceptButton).width(120f).padRight(8f);
        buttons.add(rejectButton).width(120f);
        card.add(buttons).padBottom(4f).row();
        card.add(okButton).width(120f);

        Table anchor = new Table();
        anchor.setFillParent(true);
        anchor.bottom().right();
        anchor.add(card).pad(24f);
        addActor(anchor);

        hideCard();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Register a listener that will be notified when an outgoing-invite result
     * is processed. Call setInviteListener(null) when LobbyMenu is destroyed.
     */
    public void setInviteListener(InviteListener listener) {
        this.inviteListener = listener;
    }

    public void notifyInviteSent(String targetUsername) {
        this.pendingInviteTarget = targetUsername;
    }

    public void notifyInviteCancelled() {
        this.pendingInviteTarget = null;
        if (mode == OverlayMode.INVITE_ACCEPTED || mode == OverlayMode.INVITE_REJECTED) {
            hideCard();
        }
    }

    public void notifyJoinedRandomQueue() {
        this.inRandomQueue = true;
    }

    public void notifyLeftRandomQueue() {
        this.inRandomQueue = false;
        if (mode == OverlayMode.MATCH_FOUND) {
            hideCard();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // act() — polling heartbeat
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void act(float delta) {
        super.act(delta);

        if (App.getGameSession() != null) return;
        if (mode != OverlayMode.HIDDEN) return;

        pollTimer += delta;
        if (pollTimer < POLL_INTERVAL) return;
        pollTimer = 0f;

        // 1. Incoming invite
        String inviter = lobbyService.checkIncomingInvite();
        if (inviter != null) {
            showIncomingInvite(inviter);
            return;
        }

        // 2. Result of the invite WE sent
        if (pendingInviteTarget != null) {
            String result = lobbyService.checkInviteResult();
            if (result != null) {
                String target = pendingInviteTarget;
                pendingInviteTarget = null; // clear before callbacks

                if ("ACCEPTED".equals(result)) {
                    // Notify the lobby menu first so it resets its UI
                    if (inviteListener != null) inviteListener.onInviteAccepted(target);
                    showInviteAccepted(target);
                } else {
                    // Notify the lobby menu so it resets its UI immediately,
                    // before the player even dismisses the card
                    if (inviteListener != null) inviteListener.onInviteRejected(target);
                    showInviteRejected(target);
                }
            }
        }

        // 3. Random match result
        if (inRandomQueue) {
            String opponent = lobbyService.checkRandomMatch();
            if (opponent != null) {
                inRandomQueue = false;
                showMatchFound(opponent);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Card content
    // ─────────────────────────────────────────────────────────────────────────

    private void showIncomingInvite(String inviterUsername) {
        mode = OverlayMode.INCOMING_INVITE;
        titleLabel.setText("⚔  Game Invite!");
        bodyLabel.setText(inviterUsername + " wants to play against you.");
        acceptButton.setText("✔  Accept");
        acceptButton.getLabel().setColor(new Color(0.3f, 0.9f, 0.3f, 1f));
        rejectButton.setText("✗  Reject");
        rejectButton.getLabel().setColor(new Color(0.9f, 0.3f, 0.3f, 1f));
        acceptButton.setVisible(true);
        rejectButton.setVisible(true);
        okButton.setVisible(false);
        showCard();
    }

    private void showInviteAccepted(String targetUsername) {
        mode = OverlayMode.INVITE_ACCEPTED;
        titleLabel.setText("✔  Invite Accepted!");
        bodyLabel.setText(targetUsername + " accepted!\nGet ready to play.");
        acceptButton.setVisible(false);
        rejectButton.setVisible(false);
        okButton.setVisible(true);
        showCard();
    }

    private void showInviteRejected(String targetUsername) {
        mode = OverlayMode.INVITE_REJECTED;
        titleLabel.setText("✗  Invite Rejected");
        bodyLabel.setText(targetUsername + " declined your invite.");
        acceptButton.setVisible(false);
        rejectButton.setVisible(false);
        okButton.setVisible(true);
        showCard();
    }

    private void showMatchFound(String opponentUsername) {
        mode = OverlayMode.MATCH_FOUND;
        titleLabel.setText("🎲  Match Found!");
        bodyLabel.setText("Your opponent:\n" + opponentUsername + "\nReady to play?");
        acceptButton.setText("▶  Let's go!");
        acceptButton.getLabel().setColor(new Color(0.3f, 0.9f, 0.3f, 1f));
        rejectButton.setText("✕  Bail out");
        rejectButton.getLabel().setColor(new Color(0.9f, 0.3f, 0.3f, 1f));
        acceptButton.setVisible(true);
        rejectButton.setVisible(true);
        okButton.setVisible(false);
        showCard();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Button handlers
    // ─────────────────────────────────────────────────────────────────────────

    private void onAccept() {
        switch (mode) {
            case INCOMING_INVITE -> {
                String error = lobbyService.respondToInvite(true);
                hideCard();
                if (error != null) {
                    NotificationCenter.error(error);
                } else {
                    startMatch();
                }
            }
            case MATCH_FOUND -> {
                hideCard();
                startMatch();
            }
            default -> hideCard();
        }
    }

    private void onReject() {
        switch (mode) {
            case INCOMING_INVITE -> {
                lobbyService.respondToInvite(false);
                hideCard();
                NotificationCenter.info("Invite declined.");
            }
            case MATCH_FOUND -> {
                lobbyService.leaveRandomQueue();
                hideCard();
                NotificationCenter.info("Bailed out of match.");
            }
            default -> hideCard();
        }
    }

    private void onOk() {
        OverlayMode previous = mode;
        hideCard();
        if (previous == OverlayMode.INVITE_ACCEPTED) {
            startMatch();
        }
        // INVITE_REJECTED: dismiss only — lobby is already reset by the callback
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Match start
    // ─────────────────────────────────────────────────────────────────────────

    private void startMatch() {
        App.setMenuState(MenuState.GAME);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Visibility
    // ─────────────────────────────────────────────────────────────────────────

    private void showCard() {
        setTouchable(Touchable.childrenOnly);
        card.clearActions();
        card.getColor().a = 0f;
        card.setVisible(true);
        card.addAction(fadeIn(0.22f, Interpolation.fade));
    }

    private void hideCard() {
        mode = OverlayMode.HIDDEN;
        setTouchable(Touchable.disabled);
        card.clearActions();
        card.addAction(sequence(
                fadeOut(0.18f, Interpolation.fade),
                run(() -> card.setVisible(false))
        ));
    }
}