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

        TextButton gridDebugBtn = new TextButton("Grid", skin, "default");
        gridDebugBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DebugOverlay.toggleGrid();
            }
        });

        TextButton hitboxDebugBtn = new TextButton("Hitbox", skin, "default");
        hitboxDebugBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DebugOverlay.toggleHitboxes();
            }
        });

        add(addSunBtn).height(35f).padRight(5f);
        add(addPfBtn).height(35f).padRight(5f);
        add(gridDebugBtn).height(35f).padRight(5f);
        add(hitboxDebugBtn).height(35f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        boolean isDebug = true;
        setVisible(isDebug);
    }
}