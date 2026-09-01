package com.ussr.pvz.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.model.App;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.LobbyService;
import pvz.libpvz.textures.TextureBank;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Stage-level overlay that shows incoming invite alerts and outgoing-invite
 * results from any screen.
 *
 * Styled with the same TextureBank/atlas approach used by SettingMenu:
 * NinePatch panel backgrounds, TextureRegion images and ImageButtons.
 *
 * Bug fix: the card now correctly disappears when a game session starts,
 * regardless of which OverlayMode is active (not just WAITING_FOR_SERVER).
 */
public class GlobalInviteOverlay extends Table {

    // ── Atlas texture keys (same atlas as SettingMenu) ────────────────────────

    private static final String CONTENT_PANEL  = "IMAGE_UI_SETTINGS_CONTENT_PANEL";
    private static final String ROW_LARGE      = "IMAGE_UI_SETTINGS_ROW_LARGE";
    private static final String VALUE_PANEL    = "IMAGE_UI_SETTINGS_VALUE_PANEL";
    private static final String ACCEPT_BUTTON  = "button_green";
    private static final String REJECT_BUTTON  = "button_orange";

    public String getIncomingInviter() {
        return incomingInviter;
    }

    public void setIncomingInviter(String incomingInviter) {
        this.incomingInviter = incomingInviter;
    }

    // ── Listener ──────────────────────────────────────────────────────────────

    public interface InviteListener {
        void onInviteAccepted(String opponentUsername);
        void onInviteRejected(String opponentUsername);
    }

    // ── Polling ───────────────────────────────────────────────────────────────

    private static final float POLL_INTERVAL = 2.5f;
    private float pollTimer = 0f;

    // ── Internal state ────────────────────────────────────────────────────────

    private enum OverlayMode {
        HIDDEN,
        INCOMING_INVITE,
        INVITE_REJECTED,
        WAITING_FOR_SERVER
    }

    private OverlayMode mode                = OverlayMode.HIDDEN;
    private String      pendingInviteTarget;
    private String      incomingInviter;
    private boolean     inRandomQueue       = false;
    private boolean     justFinishedSession = false;

    private InviteListener inviteListener;

    // ── Services ──────────────────────────────────────────────────────────────

    private final LobbyService lobbyService = new LobbyService();
    private final TextureBank  textures;
    private final TextureAtlas lobbyAtlas;

    // ── Widgets ───────────────────────────────────────────────────────────────

    private final Table       card;
    private final Label       titleLabel;
    private final Label       bodyLabel;
    private final ImageButton acceptButton;
    private final ImageButton rejectButton;
    private final ImageButton okButton;
    private final Label       acceptLabel;
    private final Label       rejectLabel;
    private final Label       okLabel;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public GlobalInviteOverlay(Skin skin) {
        this.textures = new TextureBank("768", Gdx.files.local("pvz-assets"));
        this.lobbyAtlas = new TextureAtlas(Gdx.files.local(
                "assets/multi player lobby/izombie_lobby_sprites.atlas"));
        setFillParent(true);
        setTouchable(Touchable.disabled);
        card = new Table();
        card.setBackground(panelDrawable(CONTENT_PANEL, 28, 28, 28, 28));
        card.pad(10f, 14f, 10f, 14f);
        titleLabel = new Label("", skin, "medium_outline");
        titleLabel.setAlignment(Align.center);
        Stack titleRow = new Stack();
        Image titleBg = image(VALUE_PANEL);
        titleBg.setScaling(Scaling.stretchX);
        titleBg.setTouchable(Touchable.disabled);
        titleRow.add(titleBg);
        titleRow.add(titleLabel);
        bodyLabel = new Label("", skin, "default");
        bodyLabel.setWrap(true);
        bodyLabel.setAlignment(Align.center);
        Table bodyCard = new Table();
        bodyCard.setBackground(panelDrawable(ROW_LARGE, 24, 24, 24, 24));
        bodyCard.pad(8f, 12f, 8f, 12f);
        bodyCard.add(bodyLabel).width(250f);
        acceptLabel = new Label("✔  Accept", skin, "default");
        acceptLabel.setColor(new Color(0.25f, 0.95f, 0.35f, 1f));
        acceptButton = lobbyButton(ACCEPT_BUTTON);
        acceptButton.addListener(click(this::onAccept));
        rejectLabel = new Label("✗  Reject", skin, "default");
        rejectLabel.setColor(new Color(0.95f, 0.30f, 0.25f, 1f));
        rejectButton = lobbyButton(REJECT_BUTTON);
        rejectButton.addListener(click(this::onReject));
        okLabel = new Label("OK", skin, "default");
        okLabel.setAlignment(Align.center);
        okButton = lobbyButton(REJECT_BUTTON);
        okButton.addListener(click(this::onOk));
        Table buttons = new Table();
        buttons.add(labeledButton(acceptButton, acceptLabel)).width(108f).height(44f).padRight(7f);
        buttons.add(labeledButton(rejectButton, rejectLabel)).width(108f).height(44f);
        buttons.add(labeledButton(okButton,     okLabel    )).width(108f).height(44f);
        card.add(titleRow ).width(280f).height(42f).padBottom(8f).row();
        card.add(bodyCard ).padBottom(9f).row();
        card.add(buttons  ).padBottom(2f).row();
        Table anchor = new Table();
        anchor.setFillParent(true);
        anchor.bottom().right();
        anchor.add(card).pad(18f);
        addActor(anchor);
        hideCard();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    public void setInviteListener(InviteListener listener) {
        this.inviteListener = listener;
    }

    public void notifyInviteSent(String targetUsername) {
        this.pendingInviteTarget = targetUsername;
    }

    public void notifyInviteCancelled() {
        this.pendingInviteTarget = null;
        if (mode == OverlayMode.INVITE_REJECTED) hideCard();
    }

    public void notifyJoinedRandomQueue()  { this.inRandomQueue = true;  }
    public void notifyLeftRandomQueue()    { this.inRandomQueue = false; }

    // ─────────────────────────────────────────────────────────────────────────
    // act() — polling heartbeat
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void act(float delta) {
        super.act(delta);
        if (App.getGameSession() != null) {
            if (mode != OverlayMode.HIDDEN) {
                pendingInviteTarget = null;
                inRandomQueue = false;
                hideCard();
            }
            pollTimer = 0f;
            justFinishedSession = true;
            return;
        }
        if (mode != OverlayMode.HIDDEN) return;
        pollTimer += delta;
        if (pollTimer < POLL_INTERVAL) return;
        pollTimer = 0f;
        if (justFinishedSession) {
            justFinishedSession = false;
            return;
        }
        String inviter = lobbyService.checkIncomingInvite();
        if (inviter != null) {
            showIncomingInvite(inviter);
            return;
        }
        if (pendingInviteTarget != null) {
            String result = lobbyService.checkInviteResult();
            if (result != null) {
                String target = pendingInviteTarget;
                pendingInviteTarget = null;
                if ("ACCEPTED".equals(result)) {
                    if (inviteListener != null) inviteListener.onInviteAccepted(target);
                    showInviteAccepted(target);
                } else {
                    if (inviteListener != null) inviteListener.onInviteRejected(target);
                    showInviteRejected(target);
                }
            }
        }
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
        incomingInviter = inviterUsername;
        mode = OverlayMode.INCOMING_INVITE;

        titleLabel.setText("⚔  Game Invite!");
        titleLabel.setColor(new Color(0.95f, 0.82f, 0.28f, 1f));

        bodyLabel.setText(inviterUsername + " wants to play against you.");

        acceptLabel.setVisible(true);
        rejectLabel.setVisible(true);
        okLabel    .setVisible(false);
        acceptButton.setVisible(true);
        rejectButton.setVisible(true);
        okButton    .setVisible(false);

        showCard();
    }

    private void showInviteAccepted(String targetUsername) {
        hideCard();
    }

    private void showInviteRejected(String targetUsername) {
        mode = OverlayMode.INVITE_REJECTED;

        titleLabel.setText("✗  Invite Rejected");
        titleLabel.setColor(new Color(0.95f, 0.35f, 0.25f, 1f));

        bodyLabel.setText(targetUsername + " declined your invite.");

        acceptButton.setVisible(false);
        rejectButton.setVisible(false);
        okButton    .setVisible(true);
        acceptLabel .setVisible(false);
        rejectLabel .setVisible(false);
        okLabel     .setVisible(true);

        showCard();
    }

    private void showMatchFound(String opponentUsername) {
        hideCard();
    }
    // ─────────────────────────────────────────────────────────────────────────
    // Button handlers
    // ─────────────────────────────────────────────────────────────────────────

    private void onAccept() {
        if (mode != OverlayMode.INCOMING_INVITE) return;

        String error = lobbyService.respondToInvite(true);
        if (error != null) {
            incomingInviter = null;
            hideCard();
            NotificationCenter.error(error);
            return;
        }

        incomingInviter = null;
        hideCard();
    }

    private void onReject() {
        if (mode != OverlayMode.INCOMING_INVITE) return;

        String error = lobbyService.respondToInvite(false);
        incomingInviter = null;
        hideCard();

        if (error != null) NotificationCenter.error(error);
        else               NotificationCenter.info("Invite declined.");
    }

    private void onOk() {
        if (mode == OverlayMode.INVITE_REJECTED) hideCard();
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
        incomingInviter = null;
        setTouchable(Touchable.disabled);
        card.clearActions();
        card.addAction(sequence(
                fadeOut(0.18f, Interpolation.fade),
                run(() -> card.setVisible(false))
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Widget helpers (mirrors SettingMenu pattern)
    // ─────────────────────────────────────────────────────────────────────────

    private Image image(String name) {
        Image i = new Image(required(name));
        i.setScaling(Scaling.fit);
        return i;
    }

    private NinePatchDrawable panelDrawable(String name, int left, int right, int top, int bottom) {
        return new NinePatchDrawable(new NinePatch(required(name), left, right, top, bottom));
    }

    /**
     * An ImageButton with tab-style up/down states (mirrors SettingMenu's imageButton helper).
     * The visible label is layered on top separately via {@link #labeledButton}.
     */
    /** A scalable button from the dedicated lobby atlas (its artwork has no baked caption). */
    private ImageButton lobbyButton(String name) {
        NinePatchDrawable up = new NinePatchDrawable(lobbyAtlas.createPatch(name));
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = up;
        style.down = up.tint(new Color(0.78f, 0.78f, 0.78f, 1f));
        return new ImageButton(style);
    }

    /**
     * Stacks a label centred over an ImageButton so the button shows both the
     * atlas texture and a readable text label.
     */
    private Stack labeledButton(ImageButton button, Label label) {
        label.setAlignment(Align.center);
        label.setTouchable(Touchable.disabled);
        Stack s = new Stack();
        s.add(button);
        Table centred = new Table();
        centred.setTouchable(Touchable.disabled);
        centred.add(label).center();
        s.add(centred);
        return s;
    }

    private TextureRegion required(String name) {
        TextureRegion region = textures.region(name);
        if (region == null) throw new IllegalStateException("Missing overlay texture: " + name);
        return region;
    }

    private ClickListener click(Runnable r) {
        return new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { r.run(); }
        };
    }
}