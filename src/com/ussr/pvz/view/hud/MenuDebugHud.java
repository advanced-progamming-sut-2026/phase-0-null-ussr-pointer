package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.model.App;

public class MenuDebugHud extends Table {

    public MenuDebugHud(Skin skin) {
        setFillParent(true);
        bottom().left();
        setTouchable(Touchable.childrenOnly);

        TextButton addCoinBtn = new TextButton("+500 Coin", skin, "default");
        addCoinBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (App.getAccount() != null)
                    App.getAccount().getAdventureProgress().addCoin(500);
            }
        });

        TextButton addGemBtn = new TextButton("+50 Gem", skin, "default");
        addGemBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (App.getAccount() != null)
                    App.getAccount().getAdventureProgress().addGem(50);
            }
        });

        add(addCoinBtn).height(35f).pad(12f).padRight(5f);
        add(addGemBtn).height(35f).pad(12f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // Only show in menus (not during active gameplay, DebugToolsWidget handles that)
        boolean inGame = App.getGameSession() != null;
        boolean show   = App.isDebugModeEnabled() && !inGame;
        setVisible(show);
        setTouchable(show ? Touchable.childrenOnly : Touchable.disabled);
    }
}