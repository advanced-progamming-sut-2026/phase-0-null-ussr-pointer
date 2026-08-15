package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.model.App;

public class GlobalMenuHud extends Table {
    private final Label coinLabel;
    private final Label diamondLabel;
    private final ImageButton backButton;

    private Runnable backAction;

    private final Skin skin;

    public GlobalMenuHud(Skin skin) {
        this.skin = skin;
        setFillParent(true);
        top();

        coinLabel = new Label("", skin);
        diamondLabel = new Label("", skin);
        Image coinIcon = new Image(
                skin.getDrawable("image_ui_generic_coin_icon_small")
        );
        Image gemIcon = new Image(
                skin.getDrawable("image_ui_generic_gem_icon_small")
        );

        Table coinCounter = createCurrencyCounter(coinIcon, coinLabel, skin);
        Table gemCounter  = createCurrencyCounter(gemIcon,  diamondLabel, skin);
        backButton = createBackButton(skin);

        Table currencyLayer   = createCurrencyLayer(coinCounter, gemCounter);
        Table navigationLayer = createNavigationLayer();

        Stack layers = new Stack();
        layers.add(currencyLayer);
        layers.add(navigationLayer);
        add(layers).grow();   // ← this is the ONLY add() call on `this`

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (backAction != null) backAction.run();
            }
        });

        setVisible(false);
    }

    private Table createCurrencyLayer(
            Table coinCounter,
            Table gemCounter
    ) {
        Table layer = new Table();
        layer.top().left();
        layer.add(coinCounter).height(48f).pad(8f);
        layer.add(gemCounter).height(48f).pad(8f);
        return layer;
    }

    private Table createNavigationLayer() {
        Table layer = new Table();
        layer.bottom().left();
        layer.add(backButton).size(61f).pad(12f);
        return layer;
    }

    private ImageButton createBackButton(Skin skin) {
        ImageButton.ImageButtonStyle style =
                new ImageButton.ImageButtonStyle();

        style.imageUp = skin.getDrawable(
                "image_ui_mainmenu_back_btn_normal"
        );

        style.imageDown = skin.getDrawable(
                "image_ui_mainmenu_back_btn_pressed"
        );

        return new ImageButton(style);
    }

    private Table createCurrencyCounter(
            Image icon,
            Label valueLabel,
            Skin skin
    ) {
        Table counter = new Table();

        counter.setBackground(
                skin.getDrawable("image_ui_hud_ingame_background_3slice")
        );

        counter.add(icon)
                .size(32f)
                .padLeft(8f)
                .padRight(5f);

        counter.add(valueLabel)
                .minWidth(65f)
                .padRight(10f);

        return counter;
    }

    public void configure(
            boolean visible,
            int coins,
            int diamonds,
            Runnable backAction
    ) {
        setVisible(visible);
        setTouchable(visible ? Touchable.childrenOnly : Touchable.disabled);

        coinLabel.setText(coins);
        diamondLabel.setText(diamonds);

        this.backAction = backAction;
        backButton.setVisible(backAction != null);
    }

    public void updateCurrencies(int coins, int diamonds) {
        coinLabel.setText(coins);
        diamondLabel.setText(diamonds);
    }

    public void setExitMode(boolean exitMode) {
        ImageButton.ImageButtonStyle style =
                new ImageButton.ImageButtonStyle();

        if (exitMode) {
            style.imageUp = skin.getDrawable(
                    "image_ui_generic_close_circle"
            );
            style.imageDown = skin.getDrawable(
                    "image_ui_generic_close_circle_down"
            );
        } else {
            style.imageUp = skin.getDrawable(
                    "image_ui_mainmenu_back_btn_normal"
            );
            style.imageDown = skin.getDrawable(
                    "image_ui_mainmenu_back_btn_pressed"
            );
        }

        backButton.setStyle(style);
    }
}
