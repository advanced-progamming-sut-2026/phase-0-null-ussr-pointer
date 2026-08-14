package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;

public class InGameCurrencyHud extends Table {

    private final Label coinLabel;
    private final Label gemLabel;

    public InGameCurrencyHud(Skin skin) {
        setTouchable(Touchable.disabled);

        coinLabel = new Label("", skin);
        gemLabel  = new Label("", skin);

        Image coinIcon = new Image(skin.getDrawable("image_ui_generic_coin_icon_small"));
        Image gemIcon  = new Image(skin.getDrawable("image_ui_generic_gem_icon_small"));

        Table coinCounter = buildCounter(skin, coinIcon, coinLabel);
        Table gemCounter  = buildCounter(skin, gemIcon,  gemLabel);

        // Vertical column, left-aligned
        left();
        add(coinCounter).height(40f).left().row();
        add(gemCounter) .height(40f).left().padTop(4f).row();
    }

    private Table buildCounter(Skin skin, Image icon, Label valueLabel) {
        Table counter = new Table();
        counter.setBackground(skin.getDrawable("image_ui_hud_ingame_background_3slice"));
        counter.add(icon)       .size(26f).padLeft(6f).padRight(4f);
        counter.add(valueLabel) .minWidth(52f).padRight(8f);
        return counter;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        Account account = App.getAccount();
        if (account == null) return;

        coinLabel.setText(account.getAdventureProgress().getCoin());
        gemLabel .setText(account.getAdventureProgress().getGem());
    }
}