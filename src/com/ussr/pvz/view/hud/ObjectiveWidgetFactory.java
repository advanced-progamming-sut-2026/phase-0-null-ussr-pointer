package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.behavior.BeghouledBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.view.util.WhitePixel;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveWidgetFactory {

    public record ObjectiveWidgets(Actor topBarWidget, Actor lawnOverlayWidget) {}

    // -------------------------------------------------------------------------
    // Public factory
    // -------------------------------------------------------------------------

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
                case "DeadlineBehavior":
                    overlay.addActor(new DeadlineLineActor(session));
                    break;
                case "BossBehavior":
                    topBar.add(new BossHealthBarActor(
                            (com.ussr.pvz.model.level.behavior.BossBehavior) behavior
                    )).width(380f).height(26f).pad(10f);
                    break;
                case "BeghouledBehavior":
                    topBar.add(new BeghouledMatchCounter(
                            (BeghouledBehavior) behavior, skin
                    )).pad(10f);
                    break;
                case "LoveYourPlantsBehavior":
                    topBar.add(new PlantsRemainingCounter(
                            (com.ussr.pvz.model.level.behavior.LoveYourPlantsBehavior) behavior, skin
                    )).pad(10f);
                    break;
                // TimedWarBehavior, EndlessBehavior, MeowBehavior, AllowedPlantsLost →
                // all communicated via LevelIntroOverlay, not the top bar.
            }
        }

        return new ObjectiveWidgets(topBar, overlay);
    }

    // -------------------------------------------------------------------------
    // Collect text objectives for LevelIntroOverlay
    // -------------------------------------------------------------------------

    /**
     * Returns the list of plain-text objective strings for a session.
     * Empty when no text objective applies.  Called by LevelIntroOverlay.
     */
    public static List<String> collectTextObjectives(GameSession session) {
        List<String> lines = new ArrayList<>();
        if (session == null || session.getLevel() == null) return lines;

        LevelBehavior behavior = session.getLevel().getBehavior();
        if (behavior != null) {
            String name = behavior.getClass().getSimpleName();
            switch (name) {
                case "TimedWarBehavior":
                    lines.add("Survive the timer!");
                    break;
                case "EndlessBehavior":
                case "MeowBehavior":
                    lines.add("Endless Mode — survive as long as you can!");
                    break;
                case "SaveOurSeedsBehavior":
                    lines.add("Protect the marked plants!\nIf any of them die, you lose.");
                    break;
            }
        }

        try {
            int lost = session.getLevel().getAllowedPlantsLost();
            if (lost != -1) {
                lines.add("Don't lose more than " + lost + " plant" + (lost == 1 ? "!" : "s!"));
            }
        } catch (Exception ignored) {}

        return lines;
    }

    // =========================================================================
    // BeghouledMatchCounter — live "X / Y matches" label shown in top bar
    // =========================================================================
    private static class BeghouledMatchCounter extends Table {
        private final BeghouledBehavior behavior;
        private final Label label;

        BeghouledMatchCounter(BeghouledBehavior behavior,
                              Skin skin) {
            this.behavior = behavior;
            label = new Label("", skin, "medium_outline");
            label.setAlignment(com.badlogic.gdx.utils.Align.center);
            label.setColor(new Color(1f, 0.95f, 0.3f, 1f));
            add(label);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            label.setText(behavior.getCurrentMatches() + " / " + behavior.getTargetMatches() + " matches");
        }
    }

    private static class PlantsRemainingCounter extends Table {
        private final com.ussr.pvz.model.level.behavior.LoveYourPlantsBehavior behavior;
        private final Label label;

        PlantsRemainingCounter(com.ussr.pvz.model.level.behavior.LoveYourPlantsBehavior behavior,
                               Skin skin) {
            this.behavior = behavior;
            top();
            label = new Label("", skin, "big_outline");
            label.setAlignment(com.badlogic.gdx.utils.Align.center);
            label.setColor(Color.WHITE);
            label.setFontScale(1f);
            add(label).top().padTop(6f);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            int remaining = Math.max(0,
                    behavior.getMaxAllowedDeaths() - behavior.getCounter());
            label.setText(remaining + " plant" + (remaining == 1 ? "" : "s") + " left");
        }
    }

    // =========================================================================
    // DeadlineLineActor — clean red line rendered with WhitePixel
    // =========================================================================
    private static class DeadlineLineActor extends Actor {
        private static final float LINE_WIDTH = 4f;
        private static final Color LINE_COLOR  = new Color(0.92f, 0.10f, 0.10f, 0.88f);
        private static final Color GLOW_COLOR  = new Color(1.00f, 0.30f, 0.20f, 0.32f);
        private static final float GLOW_EXTRA  = 6f; // extra width on each side for soft glow

        private final GameSession session;

        DeadlineLineActor(GameSession session) {
            this.session = session;
            setTouchable(Touchable.disabled);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            try {
                int col = session.getLevel().getDeadlineColumn();
                if (col < 0) return;

                // Use the same grid layout math the lawn widget uses,
                // so the line lands exactly on the cell boundary.
                float lineX = com.ussr.pvz.view.gameplay.LawnGridLayout.cellX(col);
                float lineY = com.ussr.pvz.view.gameplay.LawnGridLayout.cellY(0);
                float lineH = com.ussr.pvz.view.gameplay.LawnGridLayout.cellY(0)
                        - com.ussr.pvz.view.gameplay.LawnGridLayout.cellY(
                        session.getLawn() != null ? session.getLawn().getRows() : 5
                );

                // Soft glow pass
                Color old = batch.getColor().cpy();
                batch.setColor(GLOW_COLOR.r, GLOW_COLOR.g, GLOW_COLOR.b,
                        GLOW_COLOR.a * parentAlpha);
                batch.draw(WhitePixel.get(),
                        lineX - GLOW_EXTRA, lineY - lineH,
                        LINE_WIDTH + GLOW_EXTRA * 2f, lineH);

                // Solid line pass
                batch.setColor(LINE_COLOR.r, LINE_COLOR.g, LINE_COLOR.b,
                        LINE_COLOR.a * parentAlpha);
                batch.draw(WhitePixel.get(), lineX, lineY - lineH, LINE_WIDTH, lineH);

                batch.setColor(old);
            } catch (Exception ignored) {}
        }
    }

    // =========================================================================
    // BossHealthBarActor — styled after WaveProgressBar; three HP segments
    // =========================================================================
    private static class BossHealthBarActor extends Actor {
        private static final Color COLOR_TRACK    = new Color(0.10f, 0.10f, 0.10f, 0.88f);
        private static final Color COLOR_BORDER   = new Color(0.00f, 0.00f, 0.00f, 1.00f);
        private static final Color COLOR_FILL     = new Color(0.88f, 0.12f, 0.12f, 1.00f);
        private static final Color COLOR_STUNNED  = new Color(0.55f, 0.70f, 0.88f, 1.00f);
        private static final Color COLOR_DIVIDER  = new Color(0.00f, 0.00f, 0.00f, 1.00f);
        private static final Color COLOR_SHINE    = new Color(1.00f, 1.00f, 1.00f, 0.18f);

        private static final float BORDER = 2f;
        private static final float DIVIDER_W = 2f;

        private final com.ussr.pvz.model.level.behavior.BossBehavior bossBehavior;

        BossHealthBarActor(com.ussr.pvz.model.level.behavior.BossBehavior b) {
            this.bossBehavior = b;
            setTouchable(Touchable.disabled);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            var ctrl = bossBehavior.getController();
            if (ctrl == null) return;

            float x = getX(), y = getY(), w = getWidth(), h = getHeight();
            float a = parentAlpha;

            // Border / track background
            rect(batch, x - BORDER, y - BORDER, w + BORDER * 2f, h + BORDER * 2f,
                    COLOR_BORDER, a);
            rect(batch, x, y, w, h, COLOR_TRACK, a);

            // HP fill
            float pct = ctrl.getMaxHp() <= 0
                    ? 0f
                    : Math.max(0f, Math.min(1f, (float) ctrl.getCurrentHp() / ctrl.getMaxHp()));
            Color fill = ctrl.isStunned() ? COLOR_STUNNED : COLOR_FILL;
            rect(batch, x, y, w * pct, h, fill, a);

            // Top shine strip (upper 25% of bar)
            float shineH = h * 0.25f;
            rect(batch, x, y + h - shineH, w * pct, shineH, COLOR_SHINE, a);

            // Segment dividers at 1/3 and 2/3
            rect(batch, x + w / 3f - DIVIDER_W / 2f, y, DIVIDER_W, h, COLOR_DIVIDER, a);
            rect(batch, x + 2f * w / 3f - DIVIDER_W / 2f, y, DIVIDER_W, h, COLOR_DIVIDER, a);
        }

        private void rect(Batch batch, float x, float y, float w, float h,
                          Color c, float parentAlpha) {
            if (w <= 0 || h <= 0) return;
            Color old = batch.getColor().cpy();
            batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
            batch.draw(WhitePixel.get(), x, y, w, h);
            batch.setColor(old);
        }
    }
}