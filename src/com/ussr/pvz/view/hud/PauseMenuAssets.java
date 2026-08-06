package com.ussr.pvz.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import pvz.libpvz.textures.TextureBank;

public final class PauseMenuAssets implements Disposable {

    private final TextureBank textures;

    private final TextureRegion blankCard;
    private final TextureRegion objectiveLeft;
    private final TextureRegion objectiveMiddle;
    private final TextureRegion objectiveRight;
    private final TextureRegion windowTopper;
    private final TextureRegion sunflowerTopper;
    private final TextureRegion sliderBolt;

    private final NinePatchDrawable cardDrawable;
    private final NinePatchDrawable cardHoverDrawable;
    private final NinePatchDrawable cardPressedDrawable;

    public PauseMenuAssets() {

        textures = new TextureBank(
                "ATLASES",
                Gdx.files.local("pvz-assets")
        );

        blankCard = textures.region("IMAGE_UI_PAUSEMENU_BLANK_CARD");
        objectiveLeft = textures.region("IMAGE_UI_PAUSEMENU_OBJECTIVE_CARD_LEFT");
        objectiveMiddle = textures.region("IMAGE_UI_PAUSEMENU_OBJECTIVE_CARD_MID");
        objectiveRight = textures.region("IMAGE_UI_PAUSEMENU_OBJECTIVE_CARD_RIGHT");
        windowTopper = textures.region("IMAGE_UI_PAUSEMENU_WINDOWTOPPER");
        sunflowerTopper = textures.region("IMAGE_UI_PAUSEMENU_SUNFLOWER_TOPPER");
        sliderBolt = textures.region("IMAGE_UI_PAUSEMENU_SLIDER_BOLT");

        NinePatch cardPatch = new NinePatch(
                blankCard,
                18,
                18,
                18,
                18
        );

        cardDrawable = new NinePatchDrawable(cardPatch);

        cardHoverDrawable = new NinePatchDrawable(cardPatch)
                .tint(new Color(
                        1f,
                        0.92f,
                        0.65f,
                        1f
                ));

        cardPressedDrawable = new NinePatchDrawable(cardPatch)
                .tint(new Color(
                        0.78f,
                        0.68f,
                        0.48f,
                        1f
                ));
    }

    public Drawable cardDrawable() {
        return cardDrawable;
    }

    public Drawable cardHoverDrawable() {
        return cardHoverDrawable;
    }

    public Drawable cardPressedDrawable() {
        return cardPressedDrawable;
    }

    public Drawable objectiveLeftDrawable() {
        return new TextureRegionDrawable(objectiveLeft);
    }

    public Drawable objectiveMiddleDrawable() {
        return new TextureRegionDrawable(objectiveMiddle);
    }

    public Drawable objectiveRightDrawable() {
        return new TextureRegionDrawable(objectiveRight);
    }

    public Drawable windowTopperDrawable() {
        return new TextureRegionDrawable(windowTopper);
    }

    public Drawable sunflowerTopperDrawable() {
        return new TextureRegionDrawable(sunflowerTopper);
    }

    public Drawable sliderBoltDrawable() {
        return new TextureRegionDrawable(sliderBolt);
    }

    public TextButton.TextButtonStyle createButtonStyle(Skin skin) {

        TextButton.TextButtonStyle style =
                new TextButton.TextButtonStyle();

        style.up = cardDrawable;
        style.over = cardHoverDrawable;
        style.down = cardPressedDrawable;

        style.font = skin
                .get("default", Label.LabelStyle.class)
                .font;

        style.fontColor =
                new Color(0.20f, 0.12f, 0.05f, 1f);

        style.overFontColor =
                new Color(0.38f, 0.18f, 0.03f, 1f);

        style.downFontColor =
                new Color(0.12f, 0.08f, 0.03f, 1f);

        return style;
    }

    @Override
    public void dispose() {
        textures.dispose();
    }
}