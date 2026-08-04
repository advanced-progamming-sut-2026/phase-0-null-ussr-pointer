package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.view.hud.InGameHud;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class ActiveGameplayView extends Table {

    private static final float TICK_RATE = 0.1f;
    private float accumulator = 0f;

    private final GameplayController controller;
    private final InGameHud inGameHud;
    private final EntityRenderLayer entityLayer;

    public ActiveGameplayView(Skin skin, TextureBank textures, PamPlayer pamPlayer) {
        setFillParent(true);
        this.controller = new GameplayController();

        Image background = createBackground(textures);
        this.entityLayer = new EntityRenderLayer(pamPlayer, textures);
        this.inGameHud = new InGameHud(skin, textures, controller);

        Stack layers = new Stack();
        layers.add(background);
        layers.add(entityLayer);
        layers.add(inGameHud);

        add(layers).grow();
    }

    private Image createBackground(TextureBank textures) {
        Chapter currentChapter = App.getLevelManager().getCurrentChapter();
        String chapterId = currentChapter != null ? currentChapter.getId() : "ancient_egypt";

        String regionKey = switch (chapterId) {
            case "big_wave_beach" -> "IMAGE_BACKGROUNDS_BEACH";
            case "dark_ages" -> "IMAGE_BACKGROUNDS_DARKAGES";
            case "frostbite_caves" -> "IMAGE_BACKGROUNDS_FROSTBITE";
            default -> "IMAGE_BACKGROUNDS_EGYPT";
        };

        Image bg = new Image();
        if (textures.region(regionKey) != null) {
            bg.setDrawable(new TextureRegionDrawable(textures.region(regionKey)));
        }

        bg.setScaling(Scaling.fill);
        bg.setTouchable(Touchable.disabled);
        return bg;
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
}