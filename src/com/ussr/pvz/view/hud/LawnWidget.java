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

            private int dragStartRow = -1;
            private int dragStartCol = -1;
            private boolean dragSwapPerformed = false;

            @Override
            public boolean touchDown(
                    InputEvent event,
                    float x,
                    float y,
                    int pointer,
                    int button
            ) {
                boolean accepted = super.touchDown(event, x, y, pointer, button);

                if (accepted && LawnGridLayout.contains(x, y)) {
                    dragStartRow = LawnGridLayout.rowAt(y);
                    dragStartCol = LawnGridLayout.columnAt(x);
                } else {
                    dragStartRow = -1;
                    dragStartCol = -1;
                }
                dragSwapPerformed = false;

                return accepted;
            }

            @Override
            public void touchDragged(
                    InputEvent event,
                    float x,
                    float y,
                    int pointer
            ) {
                super.touchDragged(event, x, y, pointer);

                if (dragSwapPerformed
                        || dragStartRow == -1
                        || !LawnGridLayout.contains(x, y)) {
                    return;
                }

                int curRow = LawnGridLayout.rowAt(y);
                int curCol = LawnGridLayout.columnAt(x);

                if (curRow == dragStartRow && curCol == dragStartCol) {
                    return;
                }

                if (Math.abs(curRow - dragStartRow) + Math.abs(curCol - dragStartCol) == 1) {
                    boolean swapped = controller.handleBeghouledDrag(
                            dragStartRow, dragStartCol, curRow, curCol
                    );
                    if (swapped) {
                        dragSwapPerformed = true;
                    }
                }
            }

            @Override
            public void clicked(
                    InputEvent event,
                    float x,
                    float y
            ) {
                if (dragSwapPerformed) {
                    dragSwapPerformed = false;
                    return;
                }
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