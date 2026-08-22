package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.behavior.CouchIZombieBehavior;
import com.ussr.pvz.view.gameplay.LawnGridLayout;
import com.ussr.pvz.view.util.WhitePixel;

public class ZombieCursorWidget extends Actor {

    private static final float BORDER_THICKNESS = 3f;
    private static final Color CURSOR_COLOR = new Color(0.3f, 1f, 0.35f, 0.9f);

    private final GameplayController controller;

    public ZombieCursorWidget(GameplayController controller) {
        this.controller = controller;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        GameSession session = App.getGameSession();
        if (session == null
                || session.getLevel() == null
                || !(session.getLevel().getBehavior() instanceof CouchIZombieBehavior)) {
            return;
        }

        int column = controller.getZombieCursorColumn();
        int row = controller.getZombieCursorRow();
        if (column < 0) return;

        float x = LawnGridLayout.cellX(column);
        float y = LawnGridLayout.cellY(row);
        float w = LawnGridLayout.CELL_WIDTH;
        float h = LawnGridLayout.CELL_HEIGHT;

        TextureRegion pixel = WhitePixel.get();
        Color oldColor = new Color(batch.getColor());
        batch.setColor(CURSOR_COLOR.r, CURSOR_COLOR.g, CURSOR_COLOR.b, CURSOR_COLOR.a * parentAlpha);

        batch.draw(pixel, x, y, w, BORDER_THICKNESS);
        batch.draw(pixel, x, y + h - BORDER_THICKNESS, w, BORDER_THICKNESS);
        batch.draw(pixel, x, y, BORDER_THICKNESS, h);
        batch.draw(pixel, x + w - BORDER_THICKNESS, y, BORDER_THICKNESS, h);

        batch.setColor(oldColor);
    }
}