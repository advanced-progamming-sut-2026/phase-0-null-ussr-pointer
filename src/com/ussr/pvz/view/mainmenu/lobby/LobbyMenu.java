package com.ussr.pvz.view.mainmenu.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.ussr.pvz.model.*;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.service.LobbyService;
import com.ussr.pvz.view.hud.GlobalInviteOverlay;
import pvz.libpvz.textures.TextureBank;
import com.badlogic.gdx.math.MathUtils;
import java.util.List;

/** Multiplayer iZombie lobby composed from its dedicated sprite atlas. */
public class LobbyMenu extends Table implements Disposable {
    private static final float POLL_SECONDS = 3f;
    private enum State { BROWSING, INVITED, SEARCHING }
    private final Skin skin;
    private final TextureBank textures;
    private final TextureAtlas atlas;
    private final LobbyService service = new LobbyService();
    private final GlobalInviteOverlay overlay;
    private State state = State.BROWSING;
    private List<String> players = List.of();
    private float timer;
    private Table playerRows;
    private Label count, status;
    private Button find, cancelInvite, cancelSearch;

    public LobbyMenu(Skin skin, GlobalInviteOverlay overlay) {
        this.skin = skin;
        this.overlay = overlay;
        textures = new TextureBank("768", Gdx.files.local("pvz-assets"));
        atlas = new TextureAtlas(Gdx.files.local(
                "assets/multi player lobby/izombie_lobby_sprites.atlas"));
        setFillParent(true);
        build();
        refresh();
    }

    private void build() {
        Stack root = new Stack();
        TextureRegion bg = textures.region("IMAGE_MAINMENU_BACKGROUND");
        Image background = bg == null ? new Image() : new Image(bg);
        background.setScaling(Scaling.fill);
        root.add(background);
        root.add(new Image(skin.newDrawable("white-pixel", new Color(0, .03f, .07f, .68f))));
        Table center = new Table();
        center.add(panel()).width(960).height(680);
        root.add(center);
        add(root).grow();
    }

    private Actor panel() {
        Table t = new Table();
        t.pad(12, 14, 12, 14);
        t.add(title()).colspan(2).growX().height(78).padBottom(7).row();
        t.add(playerColumn()).width(350).growY().padRight(10);
        t.add(matchColumn()).grow().row();
        t.add(footer()).colspan(2).growX().height(60).padTop(7);
        return t;
    }

    private Actor title() {
        Table t = new Table(); t.setBackground(patch("header_stone"));
        t.add().width(60);
        t.add(label("MULTIPLAYER LOBBY", "medium_outline", Color.WHITE)).expandX();
        ImageButton close = imageButton("button_close"); close.addListener(click(this::leave));
        t.add(close).size(60);
        return t;
    }

    private Table playerColumn() {
        Table col = new Table(); col.top();
        col.add(header("icon_players_online", "ONLINE PLAYERS", false)).growX().height(64).row();
        Table paper = paper(); paper.pad(16);
        Table headings = new Table();
        headings.add(dark("PLAYER")).expandX().left(); headings.add(dark("STATUS")).width(100);
        paper.add(headings).growX().height(28).row();
        playerRows = new Table(); playerRows.top();
        ScrollPane scroll = new ScrollPane(playerRows, skin); scroll.setFadeScrollBars(false);
        paper.add(scroll).grow().row();
        Stack badge = new Stack(); badge.add(stretch("header_stone"));
        Table bc = new Table(); bc.add(icon("icon_players_online")).size(42, 28).padRight(7);
        count = label("0 ONLINE", "medium_outline", Color.WHITE); bc.add(count); badge.add(bc);
        paper.add(badge).width(210).height(50).padTop(4);
        col.add(paper).grow(); return col;
    }

    private Table matchColumn() {
        Table col = new Table(); col.top();
        col.add(header("icon_speaker", "QUICK MATCH", true)).growX().height(64).row();
        Table quick = paper(); quick.pad(10, 18, 10, 18);
        quick.add(icon("badge_izombie")).size(125, 140).padRight(12);
        Table copy = new Table();
        Label desc = dark("Jump into an iZombie match\nwith a random online player.");
        desc.setAlignment(Align.left); copy.add(desc).growX().left().padBottom(10).row();
        find = textButton("button_green", "FIND iZOMBIE MATCH");
        find.addListener(click(this::inviteRandomPlayer)); copy.add(find).width(320).height(60).left();
        quick.add(copy).growX(); col.add(quick).growX().height(166).row();
        Table cards = new Table();
        cards.add(details()).grow().padRight(8); cards.add(rules()).grow();
        col.add(cards).grow().padTop(8); return col;
    }

    private Table details() {
        Table c = card("badge_izombie", "MATCH DETAILS");
        addInfo(c, "badge_izombie", "MODE", "iZOMBIE");
        addInfo(c, "icon_players_pair", "PLAYERS", "2 PLAYERS");
        addInfo(c, "icon_objective", "MATCH", "RANDOM"); return c;
    }

    private Table rules() {
        Table c = card("icon_objective", "iZOMBIE RULES");
        addInfo(c, "icon_players_pair", "PLAYERS", "PLANTS vs. ZOMBIES");
        addInfo(c, "icon_objective", "OBJECTIVE", "DEFEAT YOUR RIVAL");
        addInfo(c, "icon_clock", "ROUND TIME", "10 MINUTES"); return c;
    }

    private Table card(String image, String title) {
        Table c = paper(); c.top().pad(0, 10, 10, 10);
        c.add(header(image, title, false)).growX().height(52).row(); return c;
    }

    private void addInfo(Table c, String image, String key, String value) {
        Table line = new Table(); line.add(icon(image)).size(34).padRight(6);
        Table text = new Table(); text.add(dark(key)).left().row();
        text.add(label(value, "default", new Color(.32f, .16f, .42f, 1))).left();
        line.add(text).growX().left(); c.add(line).growX().padTop(8).row();
    }

    private Table footer() {
        Table f = new Table();
        ImageButton back = imageButton("button_back"); back.addListener(click(this::leave));
        f.add(back).size(58);
        status = label("", "default", new Color(.95f, .87f, .65f, 1));
        status.setAlignment(Align.center); f.add(status).expandX();
        cancelInvite = textButton("button_orange", "CANCEL INVITE");
        cancelInvite.setVisible(false); cancelInvite.addListener(click(this::cancelInvite));
        f.add(cancelInvite).width(190).height(54).padRight(5);
        cancelSearch = textButton("button_orange", "CANCEL SEARCH");
        cancelSearch.setVisible(false); cancelSearch.addListener(click(this::cancelQueue));
        f.add(cancelSearch).width(190).height(54).padRight(5);
        Button refresh = iconButton("button_orange", "icon_refresh", "REFRESH");
        refresh.addListener(click(this::refresh)); f.add(refresh).width(180).height(54); return f;
    }

    private Table header(String image, String text, boolean purple) {
        Table h = new Table(); h.setBackground(patch(purple ? "header_purple" : "header_stone"));
        h.add(icon(image)).size(37).padRight(7);
        h.add(label(text, "medium_outline", Color.WHITE)).expandX().padRight(37); return h;
    }

    @Override public void act(float delta) {
        super.act(delta); timer += delta;
        if (timer >= POLL_SECONDS) { timer = 0; refresh(); }
    }

    private void refresh() { players = service.getOnlinePlayers(); rebuild(); }

    private void rebuild() {
        playerRows.clearChildren(); count.setText(players.size() + " ONLINE");
        boolean disabled = players.isEmpty() || state != State.BROWSING;
        find.setDisabled(disabled); find.setColor(disabled ? Color.GRAY : Color.WHITE);
        if (players.isEmpty()) {
            Label empty = dark("No other players online."); empty.setAlignment(Align.center);
            playerRows.add(empty).growX().padTop(28); return;
        }
        for (String name : players) playerRows.add(playerRow(name)).growX().height(56).row();
    }

    private Table playerRow(String name) {
        Table r = new Table(); r.pad(3);
        r.add(label("●", "default", new Color(.18f, .65f, .08f, 1))).width(22);
        r.add(dark(name)).expandX().left();
        r.add(label(state == State.BROWSING ? "IN LOBBY" : "BUSY", "default",
                new Color(.1f, .48f, .09f, 1))).width(88);
        Button invite = textButton("button_blue", "INVITE");
        invite.setDisabled(state != State.BROWSING); invite.setColor(invite.isDisabled() ? Color.GRAY : Color.WHITE);
        invite.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) {
            if (!invite.isDisabled()) sendInvite(name); }});
        r.add(invite).width(86).height(42); return r;
    }

    private void sendInvite(String name) {
        String error = service.sendInvite(name); if (error != null) { NotificationCenter.error(error); return; }
        state = State.INVITED; if (overlay != null) overlay.notifyInviteSent(name);
        status.setText("Invite sent to " + name + " — waiting..."); cancelInvite.setVisible(true); rebuild();
    }

    private void cancelInvite() {
        service.cancelInvite(); if (overlay != null) overlay.notifyInviteCancelled();
        state = State.BROWSING; cancelInvite.setVisible(false); status.setText(""); rebuild();
    }

    private void inviteRandomPlayer() {
        refresh();
        if (players.isEmpty()) {
            NotificationCenter.info("No other players are online.");
            return;
        }
        String opponent = players.get(MathUtils.random(players.size() - 1));
        sendInvite(opponent);
    }

    private void cancelQueue() {
        service.leaveRandomQueue(); if (overlay != null) overlay.notifyLeftRandomQueue();
        state = State.BROWSING; find.setVisible(true); cancelSearch.setVisible(false); status.setText(""); rebuild();
    }

    private void leave() {
        if (state == State.INVITED) { service.cancelInvite(); if (overlay != null) overlay.notifyInviteCancelled(); }
        if (state == State.SEARCHING) { service.leaveRandomQueue(); if (overlay != null) overlay.notifyLeftRandomQueue(); }
        App.setMenuState(MenuState.GAME);
    }

    private Table paper() { Table t = new Table(); t.setBackground(patch("panel_parchment")); return t; }
    private Image stretch(String name) { Image i = new Image(patch(name)); i.setScaling(Scaling.stretch); return i; }
    private Image icon(String name) { Image i = new Image(region(name)); i.setScaling(Scaling.fit); i.setTouchable(Touchable.disabled); return i; }
    private Drawable patch(String name) { return new NinePatchDrawable(atlas.createPatch(name)); }
    private TextureRegion region(String name) { TextureRegion r = atlas.findRegion(name); if (r == null) throw new IllegalStateException("Missing lobby sprite: " + name); return r; }
    private Label dark(String text) { return label(text, "default", new Color(.18f, .1f, .05f, 1)); }
    private Label label(String text, String style, Color color) { Label l = new Label(text, skin, style); l.setColor(color); return l; }
    private ImageButton imageButton(String name) {
        TextureRegionDrawable up = new TextureRegionDrawable(region(name)); ImageButton.ImageButtonStyle s = new ImageButton.ImageButtonStyle();
        s.imageUp = up; s.imageDown = up.tint(Color.LIGHT_GRAY); return new ImageButton(s);
    }
    private Button textButton(String bg, String text) {
        Button.ButtonStyle s = new Button.ButtonStyle(); s.up = patch(bg); s.down = ((NinePatchDrawable) patch(bg)).tint(Color.LIGHT_GRAY);
        Button b = new Button(s); b.add(label(text, "medium_outline", Color.WHITE)).grow(); return b;
    }
    private Button iconButton(String bg, String image, String text) {
        Button b = textButton(bg, ""); b.clearChildren(); b.add(icon(image)).size(36).padRight(5); b.add(label(text, "medium_outline", Color.WHITE)); return b;
    }
    private ClickListener click(Runnable action) { return new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { action.run(); }}; }

    @Override public void dispose() { atlas.dispose(); }
}
