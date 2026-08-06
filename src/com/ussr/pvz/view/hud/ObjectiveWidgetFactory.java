package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import pvz.libpvz.textures.TextureBank;

/**
 * Generates decoupled sub-widgets for arbitrary level behaviors preventing giant conditionals in the HUD.
 */
public class ObjectiveWidgetFactory {

    public record ObjectiveWidgets(Actor topBarWidget, Actor lawnOverlayWidget) {}

    public static ObjectiveWidgets create(Skin skin, TextureBank textures) {
        GameSession session = App.getGameSession();
        if (session == null || session.getLevel() == null) {
            return new ObjectiveWidgets(new Table(), new Table());
        }

        Table topBar = new Table();
        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.setTouchable(Touchable.disabled);

        LevelBehavior behavior = session.getLevel().getBehavior();
        if (behavior != null) {
            String behaviorName = behavior.getClass().getSimpleName();

            switch (behaviorName) {
                case "TimedWarBehavior":
                    topBar.add(new Label("Survive the timer!", skin, "medium_outline")).pad(10f);
                    break;
                case "EndlessBehavior":
                case "MeowBehavior":
                    topBar.add(new Label("Endless Mode Active", skin, "medium_outline")).pad(10f);
                    break;
                case "DeadlineBehavior":
                    overlay.addActor(new DeadlineLineActor(session, textures));
                    break;
                case "BossBehavior":
                    topBar.add(new BossHealthBarActor(
                            (com.ussr.pvz.model.level.behavior.BossBehavior) behavior
                    )).width(400f).height(28f).pad(10f);
                    break;
            }
        }

        try {
            if (session.getLevel().getAllowedPlantsLost() != -1) {
                topBar.add(new Label("Don't lose more than " + session.getLevel().getAllowedPlantsLost() + " plants!", skin, "medium_outline")).padLeft(15f);
            }
        } catch (Exception ignored) {}

        return new ObjectiveWidgets(topBar, overlay);
    }

    private static class DeadlineLineActor extends Actor {
        private final GameSession session;
        private final TextureRegion redLine;

        public DeadlineLineActor(GameSession session, TextureBank textures) {
            this.session = session;
            setTouchable(Touchable.disabled);
            // TODO-ASSET: image_ui_hud_deadline_line, replace with real red-line atlas region
            this.redLine = textures.region("IMAGE_UI_HUD_DEADLINE_LINE");
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            try {
                int col = session.getLevel().getDeadlineColumn();
                if (col < 0 || getParent() == null) return;

                float cellW = getParent().getWidth() / 9f;
                float lineX = getParent().getX() + col * cellW;

                if (redLine != null) {
                    batch.draw(redLine, lineX, getParent().getY(), 10f, getParent().getHeight());
                }
            } catch (Exception ignored) {}
        }
    }

    private static class BossHealthBarActor extends Actor {
        private final com.ussr.pvz.model.level.behavior.BossBehavior bossBehavior;

        BossHealthBarActor(com.ussr.pvz.model.level.behavior.BossBehavior bossBehavior) {
            this.bossBehavior = bossBehavior;
            setTouchable(Touchable.disabled);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            var controller = bossBehavior.getController();
            if (controller == null) return;

            float width = getWidth();
            float height = getHeight();
            float x = getX();
            float y = getY();

            // Background track
            drawRect(batch, x, y, width, height, 0.15f, 0.15f, 0.15f, 0.9f);

            // Filled portion, proportional to current HP
            float pct = controller.getMaxHp() <= 0
                    ? 0f
                    : (float) controller.getCurrentHp() / controller.getMaxHp();
            float fillColor = controller.isStunned() ? 0.6f : 0.85f;
            drawRect(batch, x, y, width * pct, height,
                    fillColor, controller.isStunned() ? 0.75f : 0.1f, 0.1f, 1f);

            // Two divider lines marking the 3 segment boundaries
            drawRect(batch, x + width / 3f - 1f, y, 2f, height, 0f, 0f, 0f, 1f);
            drawRect(batch, x + (2 * width) / 3f - 1f, y, 2f, height, 0f, 0f, 0f, 1f);
        }

        private void drawRect(Batch batch, float x, float y, float w, float h,
                              float r, float g, float b, float a) {
            if (w <= 0 || h <= 0) return;
            com.badlogic.gdx.graphics.Color old = batch.getColor().cpy();
            batch.setColor(r, g, b, a);
            batch.draw(com.ussr.pvz.view.util.WhitePixel.get(), x, y, w, h);
            batch.setColor(old);
        }
    }
}