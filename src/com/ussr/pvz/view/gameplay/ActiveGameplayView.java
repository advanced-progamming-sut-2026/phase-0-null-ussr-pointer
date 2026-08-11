package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.ussr.pvz.audio.AudioManager;
import com.ussr.pvz.audio.GameplayMusicDirector;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.model.level.behavior.BeghouledBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.model.level.behavior.VaseBreakerBehavior;
import com.ussr.pvz.view.components.LawnBackgroundLayer;
import com.ussr.pvz.view.hud.BeghouledOverlayWidget;
import com.ussr.pvz.view.hud.InGameHud;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import com.ussr.pvz.view.hud.HoverCursorWidget;
import com.ussr.pvz.view.hud.InGameHud;
import com.ussr.pvz.view.hud.LawnWidget;
import com.ussr.pvz.view.hud.VaseBreakerOverlayWidget;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class ActiveGameplayView extends Table implements Disposable {

    static final float TICK_RATE = 0.1f;

    /**
     * Fixed pixel width of the left lawn panel (house/sidebar area) in the background texture.
     * Decoupled from LawnGridLayout.OFFSET_X so background aspect ratio remains stable.
     */
    private static final float BACKGROUND_LEFT_PANEL_WIDTH = 280f;

    private float accumulator = 0f;

    private final GameplayController controller;
    private final InGameHud inGameHud;
    private final EntityRenderLayer entityLayer;
    private final GameplayMusicDirector musicDirector;

    public ActiveGameplayView(
            Skin skin,
            TextureBank textures,
            PamPlayer pamPlayer,
            AudioManager audioManager
    ) {
        setFillParent(true);

        this.controller = new GameplayController();
        this.musicDirector = new GameplayMusicDirector(
                audioManager,
                App.getGameSession()
        );

        Actor background = createBackground(textures);

        this.entityLayer = new EntityRenderLayer(pamPlayer, textures);

        TerrainRenderLayer terrainLayer = new TerrainRenderLayer(pamPlayer, textures);

        this.inGameHud = new InGameHud(skin, textures, controller);

        LawnWidget lawnWidget = new LawnWidget(controller);

        HoverCursorWidget hoverCursor = new HoverCursorWidget(textures, controller);

        SunRenderLayer sunLayer = new SunRenderLayer(pamPlayer);
        ItemRenderLayer itemLayer = new ItemRenderLayer(pamPlayer);
        StormRenderLayer stormRearLayer = new StormRenderLayer(pamPlayer, true);
        StormRenderLayer stormTopLayer = new StormRenderLayer(pamPlayer, false);

        Stack layers = new Stack();
        layers.add(background);
        layers.add(terrainLayer);
        layers.add(stormRearLayer);
        layers.add(entityLayer);
        layers.add(sunLayer);
        layers.add(itemLayer);
        layers.add(stormTopLayer);

        // ── Minigame overlays ─────────────────────────────────────────────────
        // These sit above the entity/item layers so they can intercept input
        // and draw highlights, but below the HUD so the HUD always reads on top.
        addMinigameOverlays(layers, textures, skin);

        layers.add(lawnWidget);   // invisible click target — must stay here for
        // standard gameplay clicks; overlays above it
        // consume their own clicks first.
        layers.add(inGameHud);
        layers.add(hoverCursor);
        layers.add(new LevelIntroOverlay(
                skin,
                textures,
                pamPlayer,
                controller,
                App.getGameSession()
        ));

        add(layers).grow();
    }

    /**
     * Inspects the current level's behavior and inserts the appropriate
     * overlay widget into the layer stack.  Only one overlay is added per session.
     */
    private void addMinigameOverlays(Stack layers, TextureBank textures, Skin skin) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLevel() == null) return;

        LevelBehavior behavior = (LevelBehavior) session.getLevel().getBehavior();

        if (behavior instanceof BeghouledBehavior) {
            layers.add(new BeghouledOverlayWidget(textures, controller));

        } else if (behavior instanceof VaseBreakerBehavior) {
            layers.add(new VaseBreakerOverlayWidget(textures, controller));
        }
        // WallnutBowling and IZombie don't need a separate overlay —
        // their input already flows through GameplayController / GameController.
    }

    private Actor createBackground(TextureBank textures) {
        Chapter currentChapter = App.getLevelManager().getCurrentChapter();

        String regionKey = currentChapter != null
                ? currentChapter.getLawnRegion()
                : "IMAGE_BACKGROUNDS_EGYPT_TEXTURE";

        // Pass fixed background width instead of LawnGridLayout.OFFSET_X
        return LawnBackgroundLayer.forGameplay(textures, regionKey, BACKGROUND_LEFT_PANEL_WIDTH);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (session == null || session.isGameOver()) return;

        if (!controller.isPaused()) {
            accumulator += delta;
            while (accumulator >= TICK_RATE) {
                session.update(TICK_RATE);
                accumulator -= TICK_RATE;
            }
        }
    }

    @Override
    public void dispose() {
        inGameHud.dispose();
    }
}