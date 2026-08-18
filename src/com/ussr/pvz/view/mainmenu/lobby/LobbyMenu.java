package com.ussr.pvz.view.mainmenu.lobby;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.LobbyService;
import com.ussr.pvz.view.hud.GlobalInviteOverlay;

import java.util.List;

/**
 * Multiplayer lobby screen.
 *
 * Invite/random-match polling is now handled by {@link GlobalInviteOverlay}
 * so notifications reach the user from any screen.  LobbyMenu only manages
 * the player list and the UI state for actions initiated here.
 */
public class LobbyMenu extends Table {

    // ── Configuration ─────────────────────────────────────────────────────────

    private static final float POLL_INTERVAL_SECONDS = 3f;

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final Skin skin;
    private final LobbyService lobbyService = new LobbyService();

    /**
     * Reference to the stage-level overlay so LobbyMenu can notify it when
     * the user performs actions (invite sent, joined queue, etc.).
     * Passed in from AppView or retrieved from the stage.
     */
    private final GlobalInviteOverlay inviteOverlay;

    // ── UI state ──────────────────────────────────────────────────────────────

    private enum LobbyState {
        BROWSING,
        INVITE_SENT,
        SEARCHING_RANDOM
    }

    private LobbyState lobbyState = LobbyState.BROWSING;
    private String pendingInviteTarget;
    private List<String> cachedPlayerList = List.of();

    // ── Polling timer (player-list only) ──────────────────────────────────────

    private float pollTimer = 0f;

    // ── Widgets ───────────────────────────────────────────────────────────────

    private Table playerListTable;
    private Label statusLabel;
    private TextButton randomButton;
    private TextButton cancelRandomButton;
    private Label playerCountLabel;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public LobbyMenu(Skin skin, GlobalInviteOverlay inviteOverlay) {
        this.skin = skin;
        this.inviteOverlay = inviteOverlay;
        setFillParent(true);
        buildUi();
        refreshPlayerList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────────────────────────────────

    private void buildUi() {
        setBackground(skin.newDrawable(
                "white-pixel",
                new Color(0.07f, 0.09f, 0.07f, 0.92f)
        ));
        pad(24f);

        // ── Header ────────────────────────────────────────────────────────────
        Table header = new Table();

        TextButton backButton = new TextButton("← Back", skin, "default");
        backButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { leaveLobby(); }
        });

        Label titleLabel = new Label("Multiplayer Lobby", skin, "big_outline");
        titleLabel.setAlignment(Align.center);

        playerCountLabel = new Label("", skin, "default");
        playerCountLabel.setAlignment(Align.right);

        header.add(backButton).left().width(120f);
        header.add(titleLabel).expandX().center();
        header.add(playerCountLabel).right().width(120f);

        add(header).fillX().padBottom(16f).row();

        // ── Content ───────────────────────────────────────────────────────────
        Table content = new Table();
        content.add(buildPlayerListColumn()).width(620f).growY().padRight(20f);
        content.add(buildRightColumn()).width(340f).growY();
        add(content).grow().row();
    }

    private Table buildPlayerListColumn() {
        Table col = new Table();

        col.add(new Label("Online Players", skin, "medium_outline"))
                .left().padBottom(8f).row();

        playerListTable = new Table();
        playerListTable.top();

        ScrollPane scroll = new ScrollPane(playerListTable, skin);
        scroll.setFadeScrollBars(false);
        col.add(scroll).grow().padBottom(8f).row();

        TextButton refreshButton = new TextButton("↺  Refresh", skin, "default");
        refreshButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                refreshPlayerList();
            }
        });
        col.add(refreshButton).right().width(140f);

        return col;
    }

    private Table buildRightColumn() {
        Table col = new Table();
        col.top();

        col.add(new Label("Quick Match", skin, "medium_outline"))
                .left().padBottom(8f).row();

        randomButton = new TextButton("🎲  Random Opponent", skin, "default");
        randomButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                joinRandomQueue();
            }
        });
        col.add(randomButton).fillX().padBottom(6f).row();

        cancelRandomButton = new TextButton("✕  Cancel Search", skin, "default");
        cancelRandomButton.setVisible(false);
        cancelRandomButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                cancelRandomSearch();
            }
        });
        col.add(cancelRandomButton).fillX().padBottom(20f).row();

        statusLabel = new Label("", skin, "default");
        statusLabel.setWrap(true);
        statusLabel.setAlignment(Align.center);
        col.add(statusLabel).fillX().padBottom(16f).row();

        // ── "Cancel sent invite" button (hidden initially) ────────────────────
        TextButton cancelInviteButton = new TextButton("✕  Cancel Invite", skin, "default");
        cancelInviteButton.setVisible(false);
        cancelInviteButton.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                cancelSentInvite();
            }
        });
        col.add(cancelInviteButton).fillX().row();

        // Keep a reference so we can toggle it
        this.cancelInviteButtonRef = cancelInviteButton;

        return col;
    }

    // Stored so we can show/hide it without rebuilding the column
    private TextButton cancelInviteButtonRef;

    // ─────────────────────────────────────────────────────────────────────────
    // act() — player-list polling only
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
            empty.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
            playerListTable.add(empty).padTop(20f);

            // Disable random only if nobody is in the queue either — keep
            // enabled so a second player can still match with you.
            // We disable if list is empty AND we are not already searching.
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
            playerListTable.add(buildPlayerRow(username)).fillX().padBottom(4f).row();
        }
    }

    private Table buildPlayerRow(String username) {
        Table row = new Table();
        row.setBackground(skin.newDrawable(
                "white-pixel", new Color(0.12f, 0.16f, 0.12f, 0.85f)
        ));
        row.pad(6f, 10f, 6f, 10f);

        Label nameLabel = new Label(username, skin, "default");
        nameLabel.setAlignment(Align.left);

        boolean canInvite = lobbyState == LobbyState.BROWSING;
        TextButton inviteBtn = new TextButton("Invite", skin, "default");
        inviteBtn.setDisabled(!canInvite);
        if (!canInvite) inviteBtn.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));

        inviteBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!inviteBtn.isDisabled()) sendInviteTo(username);
            }
        });

        row.addListener(new ClickListener() {
            @Override public void enter(InputEvent e, float x, float y, int p, Actor f) {
                row.setBackground(skin.newDrawable(
                        "white-pixel", new Color(0.18f, 0.26f, 0.18f, 0.9f)
                ));
            }
            @Override public void exit(InputEvent e, float x, float y, int p, Actor t) {
                row.setBackground(skin.newDrawable(
                        "white-pixel", new Color(0.12f, 0.16f, 0.12f, 0.85f)
                ));
            }
        });

        row.add(nameLabel).expandX().left();
        row.add(inviteBtn).right().width(90f);
        return row;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Invite — outgoing
    // ─────────────────────────────────────────────────────────────────────────

    private void sendInviteTo(String targetUsername) {
        String error = lobbyService.sendInvite(targetUsername);
        if (error != null) {
            NotificationCenter.error(error);
            return;
        }

        pendingInviteTarget = targetUsername;
        lobbyState = LobbyState.INVITE_SENT;

        // Tell the global overlay to start polling for the result
        if (inviteOverlay != null) {
            inviteOverlay.notifyInviteSent(targetUsername);
        }

        setStatus("Invite sent to " + targetUsername + ". Waiting for reply…");
        cancelInviteButtonRef.setVisible(true);
        rebuildPlayerListUi(cachedPlayerList);
    }

    private void cancelSentInvite() {
        lobbyService.cancelInvite();

        if (inviteOverlay != null) {
            inviteOverlay.notifyInviteCancelled();
        }

        pendingInviteTarget = null;
        lobbyState = LobbyState.BROWSING;
        cancelInviteButtonRef.setVisible(false);
        setStatus("");
        rebuildPlayerListUi(cachedPlayerList);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Random matchmaking
    // ─────────────────────────────────────────────────────────────────────────

    private void joinRandomQueue() {
        String error = lobbyService.joinRandomQueue();
        if (error != null) {
            NotificationCenter.error(error);
            return;
        }

        lobbyState = LobbyState.SEARCHING_RANDOM;

        // Tell the global overlay to poll for a match result
        if (inviteOverlay != null) {
            inviteOverlay.notifyJoinedRandomQueue();
        }

        randomButton.setVisible(false);
        cancelRandomButton.setVisible(true);
        randomButton.setDisabled(false);
        randomButton.setColor(Color.WHITE);
        setStatus("Searching for a random opponent…");
        rebuildPlayerListUi(cachedPlayerList);
    }

    private void cancelRandomSearch() {
        lobbyService.leaveRandomQueue();

        if (inviteOverlay != null) {
            inviteOverlay.notifyLeftRandomQueue();
        }

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
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void setStatus(String text) {
        statusLabel.setText(text);
    }
}