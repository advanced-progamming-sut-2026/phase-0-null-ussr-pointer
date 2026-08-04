package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;

/**
 * An invisible interaction layer mapping Scene2D local coordinates
 * to precise logical grid indices for the simulation.
 */
public class LawnWidget extends Actor {

    private static final int COLUMNS = 9;
    private static final int ROWS = 5;

    public LawnWidget(GameplayController controller) {
        setTouchable(Touchable.enabled);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // 'x' and 'y' are automatically localized to this Actor's bounds by Scene2D.
                float cellWidth = getWidth() / COLUMNS;
                float cellHeight = getHeight() / ROWS;

                int gridX = (int) (x / cellWidth);

                // LibGDX origin (0,0) is bottom-left. If your logical Model uses
                // (0,0) as top-left, invert the Y coordinate:
                // int gridY = ROWS - 1 - (int) (y / cellHeight);
                int gridY = (int) (y / cellHeight);

                // Guard against edge-case out-of-bounds clicks on the absolute extreme pixels
                if (gridX >= 0 && gridX < COLUMNS && gridY >= 0 && gridY < ROWS) {
                    controller.handleGridClick(gridX, gridY);
                }
            }
        });
    }
}