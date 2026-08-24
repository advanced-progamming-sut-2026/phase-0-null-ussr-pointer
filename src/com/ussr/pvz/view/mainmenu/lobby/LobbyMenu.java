package com.ussr.pvz.view.mainmenu.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.LobbyService;
import com.ussr.pvz.view.hud.GlobalInviteOverlay;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

/**
 * Multiplayer lobby screen — styled with the same TextureBank/atlas approach
 * used by SettingMenu (NinePatch panels, TextureRegion images, ImageButtons).
 *
 * Invite/random-match polling is handled by {@link GlobalInviteOverlay}.
 */
public class LobbyMenu extends Table {

    // ── Atlas texture keys ────────────────────────────────────────────────────

    /** Shared background (same as SettingMenu). */
    private static final String BG              = "IMAGE_MAINMENU_BACKGROUND";

    /** Panel / card backgrounds. */
    private static final String CONTENT_PANEL   = "IMAGE_UI_SETTINGS_CONTENT_PANEL";
    private static final String ROW_LARGE       = "IMAGE_UI_SETTINGS_ROW_LARGE";
    private static final String TAB_DARK        = "IMAGE_UI_SETTINGS_TAB_DARK";
    private static final String TAB_GREEN       = "IMAGE_UI_SETTINGS_TAB_GREEN";

    /** Header image (reused from settings — or swap for a lobby-specific one). */

    /** Buttons. */
    private static final String CLOSE           = "IMAGE_UI_SETTINGS_CLOSE";
    private static final String APPLY           = "IMAGE_UI_SETTINGS_BUTTON_APPLY";   // "Search" action
    private static final String RESET           = "IMAGE_UI_SETTINGS_BUTTON_RESET";   // "Cancel" action

    /** Value panel — used for the online-count badge. */
    private static final String VALUE_PANEL     = "IMAGE_UI_SETTINGS_VALUE_PANEL";

    /** Icons for the right-column section headings. */
    private static final String ICON_GAMEPLAY   = "IMAGE_UI_SETTINGS_ICON_GAMEPLAY";
    private static final String ICON_AUDIO      = "IMAGE_UI_SETTINGS_ICON_AUDIO";

    // ── Configuration ─────────────────────────────────────────────────────────

    private static final float POLL_INTERVAL_SECONDS = 3f;

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final Skin        skin;
    private final TextureBank textures;
    private final LobbyService lobbyService = new LobbyService();
    private final GlobalInviteOverlay inviteOverlay;

    // ── UI state ──────────────────────────────────────────────────────────────

    private enum LobbyState { BROWSING, INVITE_SENT, SEARCHING_RANDOM }

    private LobbyState      lobbyState        = LobbyState.BROWSING;
    private String          pendingInviteTarget;
    private List<String>    cachedPlayerList  = List.of();
    private float           pollTimer         = 0f;

    // ── Widgets ───────────────────────────────────────────────────────────────

    private Table       playerListTable;
    private Label       statusLabel;
    private ImageButton randomButton;
    private ImageButton cancelRandomButton;
    private Label       playerCountLabel;
    private ImageButton cancelInviteButton;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public LobbyMenu(Skin skin, GlobalInviteOverlay inviteOverlay) {
        this.skin          = skin;
        this.inviteOverlay = inviteOverlay;
        this.textures      = new TextureBank("768", Gdx.files.local("pvz-assets"));

        setFillParent(true);
        buildUi();
        refreshPlayerList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI construction
    // ─────────────────────────────────────────────────────────────────────────

    private void buildUi() {
        // Full-screen stack: background → dim → centred panel
        Stack root = new Stack();
        root.add(background());

        Image dim = new Image(skin.newDrawable("white-pixel", new Color(0f, 0f, 0f, 0.54f)));
        dim.setTouchable(Touchable.disabled);
        root.add(dim);

        Table centre = new Table();
        centre.add(panel()).width(1080f).height(680f);
        root.add(centre);

        add(root).grow();
    }

    // ── Outer panel ───────────────────────────────────────────────────────────

    private Actor panel() {
        Table p = new Table();
        p.setBackground(panelDrawable(CONTENT_PANEL, 28, 28, 28, 28));
        p.pad(14f, 18f, 14f, 18f);

        p.add(header()).colspan(2).growX().height(100f).row();

        p.add(buildPlayerListColumn()).width(620f).growY().padRight(18f);
        p.add(buildRightColumn()).grow().row();

        p.add(footer()).colspan(2).growX().height(72f);
        return p;
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private Actor header() {
        Table t = new Table();
        t.add().width(72f); // balance the close button on the right

        // Reuse the settings header image; swap for "IMAGE_UI_LOBBY_HEADER" when available

        ImageButton close = imageButton(CLOSE);
        close.addListener(click(this::leaveLobby));
        t.add(close).size(72f).right();
        return t;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private Actor footer() {
        Table f = new Table();

        // Left: cancel invite / cancel random (context-sensitive)
        cancelInviteButton = imageButton(RESET);
        cancelInviteButton.setVisible(false);
        cancelInviteButton.addListener(click(this::cancelSentInvite));

        cancelRandomButton = imageButton(RESET);
        cancelRandomButton.setVisible(false);
        cancelRandomButton.addListener(click(this::cancelRandomSearch));

        f.add(cancelInviteButton).width(230f).height(58f).left();
        f.add(cancelRandomButton).width(230f).height(58f).left();

        f.add().growX();

        // Status label in the centre
        statusLabel = new Label("", skin, "default");
        statusLabel.setAlignment(Align.center);
        statusLabel.setColor(new Color(0.9f, 0.86f, 0.70f, 1f));
        f.add(statusLabel).expandX().center();

        f.add().growX();

        // Right: random-match button
        randomButton = imageButton(APPLY);
        randomButton.addListener(click(this::joinRandomQueue));
        f.add(randomButton).width(250f).height(68f).right();

        return f;
    }

    // ── Player-list column (left) ─────────────────────────────────────────────

    private Table buildPlayerListColumn() {
        Table col = new Table();
        col.top();

        // Section heading with icon
        col.add(sectionHeading("Online Players", ICON_GAMEPLAY))
                .fillX().padBottom(10f).row();

        playerListTable = new Table();
        playerListTable.top();

        ScrollPane scroll = new ScrollPane(playerListTable, skin);
        scroll.setFadeScrollBars(false);

        // Wrap in a ROW_LARGE-backed container so it has the same panel feel
        Table scrollCard = new Table();
        scrollCard.setBackground(panelDrawable(ROW_LARGE, 24, 24, 24, 24));
        scrollCard.add(scroll).grow().pad(8f);

        col.add(scrollCard).grow().padBottom(10f).row();

        // Refresh button (right-aligned, styled like a tab)
        Button refreshBtn = tabButton("↺  Refresh");
        refreshBtn.addListener(click(this::refreshPlayerList));
        col.add(refreshBtn).right().width(160f).height(56f);

        return col;
    }

    // ── Right column ─────────────────────────────────────────────────────────

    private Table buildRightColumn() {
        Table col = new Table();
        col.top();

        col.add(sectionHeading("Quick Match", ICON_AUDIO))
                .fillX().padBottom(10f).row();

        // Description card
        Table infoCard = new Table();
        infoCard.setBackground(panelDrawable(ROW_LARGE, 24, 24, 24, 24));
        infoCard.pad(14f, 18f, 14f, 18f);

        Label infoText = new Label(
                "Select a player from the list and press Invite,\n" +
                        "or use Quick Match to find a random opponent.",
                skin, "default"
        );
        infoText.setWrap(true);
        infoText.setAlignment(Align.center);
        infoText.setColor(new Color(0.82f, 0.78f, 0.62f, 1f));
        infoCard.add(infoText).width(300f);

        col.add(infoCard).fillX().padBottom(14f).row();

        // Online count badge
        Stack badge = new Stack();
        Image valueBg = image(VALUE_PANEL);
        valueBg.setScaling(Scaling.stretch);
        valueBg.setTouchable(Touchable.disabled);
        badge.add(valueBg);

        playerCountLabel = new Label("0 online", skin, "medium_outline");
        playerCountLabel.setAlignment(Align.center);
        playerCountLabel.setColor(new Color(0.28f, 0.14f, 0.06f, 1f));
        badge.add(playerCountLabel);

        col.add(badge).width(200f).height(58f).center().padBottom(14f).row();

        col.add().growY();
        return col;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // act() — player-list polling
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void act(float delta) {
        super.act(delta);
        pollTimer += delta;
        if (pollTimer >= POLL_INTERVAL_SECONDS) {
            pollTimer = 0f;
            refreshPlayerList();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Player list
    // ─────────────────────────────────────────────────────────────────────────

    private void refreshPlayerList() {
        List<String> players = lobbyService.getOnlinePlayers();
        cachedPlayerList = players;
        rebuildPlayerListUi(players);
    }

    private void rebuildPlayerListUi(List<String> players) {
        playerListTable.clearChildren();
        playerCountLabel.setText(players.size() + " online");

        if (players.isEmpty()) {
            Label empty = new Label("No other players online.", skin, "default");
            empty.setAlignment(Align.center);
            empty.setColor(new Color(0.55f, 0.55f, 0.55f, 1f));
            playerListTable.add(empty).padTop(24f);

            if (lobbyState != LobbyState.SEARCHING_RANDOM) {
                randomButton.setDisabled(true);
                randomButton.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
            }
            return;
        }

        if (lobbyState != LobbyState.SEARCHING_RANDOM) {
            randomButton.setDisabled(false);
            randomButton.setColor(Color.WHITE);
        }

        for (String username : players) {
            playerListTable.add(buildPlayerRow(username)).fillX().padBottom(6f).row();
        }
    }

    private Table buildPlayerRow(String username) {
        Table row = new Table();
        // Use the TAB_DARK nine-patch so rows share the same visual language as
        // the settings navigation tabs rather than a plain solid rectangle.
        row.setBackground(panelDrawable(TAB_DARK, 24, 24, 24, 24));
        row.pad(8f, 14f, 8f, 10f);

        // Avatar placeholder (coloured square using the settings value-panel)
        Stack avatar = new Stack();
        Image avatarBg = image(VALUE_PANEL);
        avatarBg.setScaling(Scaling.stretch);
        avatarBg.setTouchable(Touchable.disabled);
        Label initLabel = new Label(
                username.substring(0, 1).toUpperCase(),
                skin, "medium_outline"
        );
        initLabel.setAlignment(Align.center);
        initLabel.setColor(new Color(0.28f, 0.14f, 0.06f, 1f));
        avatar.add(avatarBg);
        avatar.add(initLabel);

        Label nameLabel = new Label(username, skin, "medium_outline");
        nameLabel.setAlignment(Align.left);
        nameLabel.setColor(new Color(0.95f, 0.90f, 0.72f, 1f));

        boolean canInvite = lobbyState == LobbyState.BROWSING;

        // Invite uses TAB_GREEN when active, TAB_DARK when disabled
        Button.ButtonStyle invStyle = new Button.ButtonStyle();
        invStyle.up      = panelDrawable(canInvite ? TAB_GREEN : TAB_DARK, 24, 24, 24, 24);
        invStyle.down    = panelDrawable(TAB_GREEN, 24, 24, 24, 24);
        invStyle.checked = invStyle.up;

        Button inviteBtn = new Button(invStyle);
        Label inviteLbl = new Label(canInvite ? "Invite" : "Busy", skin, "default");
        inviteLbl.setAlignment(Align.center);
        inviteLbl.setColor(canInvite
                ? new Color(0.25f, 0.95f, 0.35f, 1f)
                : new Color(0.5f, 0.5f, 0.5f, 1f));
        inviteBtn.add(inviteLbl);
        inviteBtn.setDisabled(!canInvite);

        inviteBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (!inviteBtn.isDisabled()) sendInviteTo(username);
            }
        });

        // Hover highlight: swap background to TAB_GREEN tinted
        row.addListener(new ClickListener() {
            @Override public void enter(InputEvent e, float x, float y, int p, Actor f) {
                row.setBackground(panelDrawable(TAB_GREEN, 24, 24, 24, 24));
            }
            @Override public void exit(InputEvent e, float x, float y, int p, Actor t) {
                row.setBackground(panelDrawable(TAB_DARK, 24, 24, 24, 24));
            }
        });

        row.add(avatar).size(42f).padRight(12f);
        row.add(nameLabel).expandX().left();
        row.add(inviteBtn).width(100f).height(42f).right();
        return row;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Invite — outgoing
    // ─────────────────────────────────────────────────────────────────────────

    private void sendInviteTo(String targetUsername) {
        String error = lobbyService.sendInvite(targetUsername);
        if (error != null) { NotificationCenter.error(error); return; }

        pendingInviteTarget = targetUsername;
        lobbyState = LobbyState.INVITE_SENT;

        if (inviteOverlay != null) inviteOverlay.notifyInviteSent(targetUsername);

        setStatus("Invite sent to " + targetUsername + ". Waiting for reply…");
        cancelInviteButton.setVisible(true);
        rebuildPlayerListUi(cachedPlayerList);
    }

    private void cancelSentInvite() {
        lobbyService.cancelInvite();
        if (inviteOverlay != null) inviteOverlay.notifyInviteCancelled();

        pendingInviteTarget = null;
        lobbyState = LobbyState.BROWSING;
        cancelInviteButton.setVisible(false);
        setStatus("");
        rebuildPlayerListUi(cachedPlayerList);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Random matchmaking
    // ─────────────────────────────────────────────────────────────────────────

    private void joinRandomQueue() {
        String error = lobbyService.joinRandomQueue();
        if (error != null) { NotificationCenter.error(error); return; }

        lobbyState = LobbyState.SEARCHING_RANDOM;
        if (inviteOverlay != null) inviteOverlay.notifyJoinedRandomQueue();

        randomButton.setVisible(false);
        cancelRandomButton.setVisible(true);
        randomButton.setDisabled(false);
        randomButton.setColor(Color.WHITE);
        setStatus("Searching for a random opponent…");
        rebuildPlayerListUi(cachedPlayerList);
    }

    private void cancelRandomSearch() {
        lobbyService.leaveRandomQueue();
        if (inviteOverlay != null) inviteOverlay.notifyLeftRandomQueue();

        lobbyState = LobbyState.BROWSING;
        randomButton.setVisible(true);
        cancelRandomButton.setVisible(false);
        setStatus("");
        rebuildPlayerListUi(cachedPlayerList);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Exit
    // ─────────────────────────────────────────────────────────────────────────

    private void leaveLobby() {
        if (lobbyState == LobbyState.INVITE_SENT) {
            lobbyService.cancelInvite();
            if (inviteOverlay != null) inviteOverlay.notifyInviteCancelled();
        }
        if (lobbyState == LobbyState.SEARCHING_RANDOM) {
            lobbyService.leaveRandomQueue();
            if (inviteOverlay != null) inviteOverlay.notifyLeftRandomQueue();
        }
        App.setMenuState(MenuState.GAME);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Widget helpers (mirrors SettingMenu pattern exactly)
    // ─────────────────────────────────────────────────────────────────────────

    /** Full-screen background image (same atlas key as SettingMenu). */
    private Image background() {
        TextureRegion region = textures.region(BG);
        Image i = region == null ? new Image() : new Image(region);
        i.setScaling(Scaling.fill);
        i.setTouchable(Touchable.disabled);
        return i;
    }

    private Image image(String name) {
        Image i = new Image(required(name));
        i.setScaling(Scaling.fit);
        return i;
    }

    private TextureRegionDrawable drawable(String name) {
        return new TextureRegionDrawable(required(name));
    }

    private NinePatchDrawable panelDrawable(String name, int left, int right, int top, int bottom) {
        return new NinePatchDrawable(new NinePatch(required(name), left, right, top, bottom));
    }

    private ImageButton imageButton(String name) {
        TextureRegionDrawable up = drawable(name);
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp   = up;
        style.imageDown = up.tint(new Color(0.82f, 0.82f, 0.82f, 1f));
        ImageButton b = new ImageButton(style);
        b.getImage().setScaling(Scaling.fit);
        return b;
    }

    /** A tab-styled text button (no texture needed for the label). */
    private Button tabButton(String text) {
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up      = panelDrawable(TAB_DARK,  24, 24, 24, 24);
        style.down    = panelDrawable(TAB_GREEN, 24, 24, 24, 24);
        style.checked = style.up;

        Button b = new Button(style);
        Label l = new Label(text, skin, "medium_outline");
        l.setAlignment(Align.center);
        b.add(l).center();
        return b;
    }

    /** Icon + label heading row (matches settings navigation style). */
    private Table sectionHeading(String text, String iconRegion) {
        Table t = new Table();
        t.setBackground(panelDrawable(TAB_DARK, 24, 24, 24, 24));
        t.pad(6f, 14f, 6f, 14f);
        t.add(image(iconRegion)).size(36f).padRight(10f);
        Label l = new Label(text, skin, "medium_outline");
        l.setAlignment(Align.left);
        t.add(l).growX().left();
        return t;
    }

    private TextureRegion required(String name) {
        TextureRegion region = textures.region(name);
        if (region == null) throw new IllegalStateException("Missing lobby texture: " + name);
        return region;
    }

    private ClickListener click(Runnable r) {
        return new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { r.run(); }
        };
    }

    private void setStatus(String text) {
        if (statusLabel != null) statusLabel.setText(text);
    }
}