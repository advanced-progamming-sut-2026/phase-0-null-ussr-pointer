package com.ussr.pvz.view.mainmenu.gamemenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.controller.GlobalController;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.view.AppMenu;

import java.util.Scanner;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;

public class GameMenu extends Table {
    private final Skin skin;


    public GameMenu(Skin skin) {
        this.skin = skin;
        setFillParent(true);
        top().right();

        buildUi();
    }

    private void buildUi() {
        Stack layers = new Stack();

        layers.add(createCenterActions());
        layers.add(createBottomActions());

        add(layers).grow();
    }

    private Table createCenterActions() {
        Table layer = new Table();
        ImageButton gameButton = createBannerButton(
                "image_ui_mainmenu_mainmenu_content_offline"
        );
        gameButton.addListener(listener(this::openGreenhouse));
        addHoverEffect(gameButton);
        layer.add(createLabeledBanner(gameButton, "Green house"))
                .size(270f, 108f);
        return layer;
    }

    private void openGreenhouse() {
        App.setMenuState(MenuState.GREENHOUSE);
    }

    private Table createBottomActions() {
        Table layer = new Table();
        layer.bottom();

        ImageButton travelLogButton = createBannerButton(
                "image_ui_mainmenu_mainmenu_content_downloading"
        );
        travelLogButton.addListener(listener(this::openShop));

        layer.add(createLabeledBanner(travelLogButton, "Shop"))
                .size(270f, 108f)
                .padBottom(24f);
        return layer;
    }

    private void openShop() {
        App.setMenuState(MenuState.SHOP);
    }

    private ImageButton createBannerButton(String drawableName) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = skin.getDrawable(drawableName);
        style.imageDown = skin.newDrawable(
                drawableName,
                new Color(0.82f, 0.82f, 0.82f, 1f)
        );
        ImageButton button = new ImageButton(style);
        addHoverEffect(button);
        return button;
    }

    private void addHoverEffect(ImageButton button) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(
                    InputEvent event,
                    float x,
                    float y,
                    int pointer,
                    Actor fromActor
            ) {
                button.clearActions();
                button.setTransform(true);
                button.setOrigin(Align.center);
                button.addAction(parallel(
                        color(
                                new Color(1f, 1f, 0.78f, 1f),
                                0.16f,
                                Interpolation.fade
                        ),
                        scaleTo(
                                1.06f,
                                1.06f,
                                0.16f,
                                Interpolation.smooth
                        )
                ));
            }

            @Override
            public void exit(
                    InputEvent event,
                    float x,
                    float y,
                    int pointer,
                    Actor toActor
            ) {
                button.clearActions();
                button.addAction(parallel(
                        color(
                                Color.WHITE,
                                0.16f,
                                Interpolation.fade
                        ),
                        scaleTo(
                                1f,
                                1f,
                                0.16f,
                                Interpolation.smooth
                        )
                ));
            }
        });
    }

    private Stack createLabeledBanner(
            ImageButton button,
            String caption
    ) {
        Stack banner = new Stack();
        banner.add(button);

        Label label = new Label(caption, skin, "medium_outline");
        label.setTouchable(Touchable.disabled);

        Table captionLayer = new Table();
        captionLayer.bottom();
        captionLayer.add(label).padBottom(7f);
        captionLayer.setTouchable(Touchable.disabled);
        banner.add(captionLayer);
        return banner;
    }

    private ChangeListener listener(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        };
    }
}