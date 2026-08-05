package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;

public class LawnWidget extends Actor {

    private static final int COLUMNS = 9;
    private static final int ROWS = 5;

    // Sync these exactly with EntityRenderLayer
    private static final float GRID_OFFSET_X = 320f;
    private static final float GRID_OFFSET_Y = 80f;
    private static final float CELL_WIDTH = 100f;
    private static final float CELL_HEIGHT = 115f;

    public LawnWidget(GameplayController controller) {
        setTouchable(Touchable.enabled);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Remove the padding to find the true relative click position
                float gridXRaw = x - GRID_OFFSET_X;
                float gridYRaw = y - GRID_OFFSET_Y;

                // Ensure the click was actually inside the grid bounds, not on the UI or house
                if (gridXRaw >= 0 && gridYRaw >= 0) {
                    int gridX = (int) (gridXRaw / CELL_WIDTH);
                    int gridY = (int) (gridYRaw / CELL_HEIGHT);

                    if (gridX < COLUMNS && gridY < ROWS) {
                        controller.handleGridClick(gridX, gridY);
                    }
                }
            }
        });
    }
}