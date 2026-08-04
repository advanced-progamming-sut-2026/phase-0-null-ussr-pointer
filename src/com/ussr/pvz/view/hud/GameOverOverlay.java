package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;

public class GameOverOverlay extends Table {

    public GameOverOverlay(Skin skin) {
        setFillParent(true);
        setTouchable(Touchable.enabled);
        setBackground(skin.newDrawable("white-pixel", new Color(0f, 0f, 0f, 0.8f)));

        Table dialog = new Table();
        if (skin.has("image_ui_dialog_asset_dialogborder_10", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            dialog.setBackground(skin.getDrawable("image_ui_dialog_asset_dialogborder_10"));
        }
        dialog.pad(40f);

        Label titleLabel = new Label("GAME OVER", skin, "big_outline");
        titleLabel.setColor(new Color(1f, 0.4f, 0.4f, 1f));
        dialog.add(titleLabel).padBottom(30f).row();

        TextButton retryBtn = new TextButton("Retry", skin, "green");
        retryBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                App.getLevelManager().startLevel(App.getGameSession().getLevel().getId());
                App.setMenuState(MenuState.CHOOSE_PLANT);
            }
        });
        dialog.add(retryBtn).width(240f).height(60f).padBottom(15f).row();

        TextButton exitBtn = new TextButton("Exit to Map", skin, "brown");
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
        // We only check if it is over. Since there's no isVictory() method, we default to the standard Game Over layout.
        if (App.getGameSession() != null && App.getGameSession().isGameOver()) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}