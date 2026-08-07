package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.view.gameplay.LawnGridLayout;

public class LawnWidget extends Actor {

    public LawnWidget(GameplayController controller) {
        setTouchable(Touchable.enabled);
        addListener(new ClickListener() {
            @Override
            public void clicked(
                    InputEvent event,
                    float x,
                    float y
            ) {
                handleLawnClick(controller, x, y);
            }
        });
    }
    private void handleLawnClick(
            GameplayController controller,
            float x,
            float y
    ) {
        if (!LawnGridLayout.contains(x, y)) {
            return;
        }
        int column = LawnGridLayout.columnAt(x);
        int row = LawnGridLayout.rowAt(y);
        controller.handleGridClick(column, row);
    }
}