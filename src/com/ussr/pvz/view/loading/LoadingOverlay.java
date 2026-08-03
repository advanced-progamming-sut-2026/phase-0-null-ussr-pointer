package com.ussr.pvz.view.loading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public final class LoadingOverlay extends Table {
    private final LoadingFlower loadingFlower;

    public LoadingOverlay(Skin skin) {
        setFillParent(true);
        setTouchable(Touchable.disabled);

        setBackground(skin.newDrawable(
                "white-pixel",
                new Color(0f, 0f, 0f, 0.72f)
        ));

        PamPlayer pamPlayer = createPamPlayer();
        loadingFlower = new LoadingFlower(pamPlayer);

        Label loadingLabel = new Label(
                "Loading...",
                skin,
                "big_outline"
        );

        add(loadingFlower)
                .size(180f, 180f)
                .padBottom(10f)
                .row();

        add(loadingLabel);

        setVisible(false);
        getColor().a = 0f;
    }

    private PamPlayer createPamPlayer() {
        FileHandle assetsFolder =
                Gdx.files.local("pvz-assets");

        TextureBank textureBank = new TextureBank(
                "768",
                assetsFolder
        );

        return new PamPlayer(
                textureBank,
                assetsFolder
        );
    }

    public void show() {
        clearActions();

        loadingFlower.restart();

        setVisible(true);
        setTouchable(Touchable.enabled);
        getColor().a = 0f;

        addAction(fadeIn(0.18f));
    }

    public void hide() {
        clearActions();

        addAction(sequence(
                fadeOut(0.18f),
                run(this::finishHiding)
        ));
    }

    private void finishHiding() {
        setVisible(false);
        setTouchable(Touchable.disabled);
    }
}