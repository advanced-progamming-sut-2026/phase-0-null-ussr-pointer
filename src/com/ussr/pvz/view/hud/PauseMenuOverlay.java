package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;

public class PauseMenuOverlay extends Table {
    private final GameplayController controller;

    public PauseMenuOverlay(Skin skin, GameplayController controller) {
        this.controller = controller;
        setFillParent(true);
        setTouchable(Touchable.enabled);
        setBackground(skin.newDrawable("white-pixel", new Color(0f, 0f, 0f, 0.7f)));

        Table dialog = new Table();
        if (skin.has("image_ui_dialog_asset_dialogborder_10", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            dialog.setBackground(skin.getDrawable("image_ui_dialog_asset_dialogborder_10"));
        }
        dialog.pad(40f);

        Label title = new Label("PAUSED", skin, "big_outline");
        dialog.add(title).padBottom(30f).row();

        TextButton resumeBtn = new TextButton("Resume", skin, "green");
        resumeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.togglePauseMenu();
            }
        });
        dialog.add(resumeBtn).width(240f).height(60f).padBottom(15f).row();

        TextButton restartBtn = new TextButton("Restart Level", skin, "brown");
        restartBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                App.getLevelManager().startLevel(App.getGameSession().getLevel().getId());
                App.setMenuState(MenuState.CHOOSE_PLANT);
            }
        });
        dialog.add(restartBtn).width(240f).height(60f).padBottom(15f).row();

        TextButton exitBtn = new TextButton("Save & Exit", skin, "brown");
        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                App.setMenuState(MenuState.LEVEL_SELECTION);
                App.setGameSession(null);
            }
        });
        dialog.add(exitBtn).width(240f).height(60f).row();

        add(dialog);
        setVisible(false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // Delegate state validation strictly to the Controller
        setVisible(App.getGameSession() != null && controller.isPaused());
    }
}