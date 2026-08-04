package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.model.App;

public class DebugToolsWidget extends Table {
    public DebugToolsWidget(Skin skin) {
        TextButton addSunBtn = new TextButton("+100 Sun", skin, "default");
        addSunBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (App.getGameSession() != null) {
                    App.getGameSession().addSun(100);
                }
            }
        });

        TextButton addPfBtn = new TextButton("+1 Food", skin, "default");
        addPfBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (App.getGameSession() != null) {
                    App.getGameSession().addPlantFood();
                }
            }
        });

        add(addSunBtn).height(35f).padRight(5f);
        add(addPfBtn).height(35f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        boolean isDebug = false;
        try {
            // Checks if the difficulty is set to debug/cheat configurations or an arbitrary global var.
            // Adjust to your precise global debug flag binding from SettingMenu.
            if (App.getAccount() != null && App.getAccount().getDifficultyLvl() == -1) {
                isDebug = true;
            }
        } catch (Exception ignored) {}

        setVisible(isDebug); // Falls back to hidden unless explicitly triggered
    }
}