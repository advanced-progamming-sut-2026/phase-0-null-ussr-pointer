package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.behavior.BeghouledBehavior;
import com.ussr.pvz.model.level.behavior.LevelBehavior;
import com.ussr.pvz.view.util.WhitePixel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Top-right HUD panel shown exclusively during the Beghouled minigame.
 *
 * <p>It reads the live board from {@link BeghouledBehavior#getActivePlantTypes()}
 * and shows one upgrade button per plant type that has an upgrade path.
 * The controller is called on click; the panel re-reads sun every frame to
 * grey out buttons the player cannot afford.</p>
 *
 * <p>The panel is invisible (and zero-size) outside Beghouled levels,
 * so it never disrupts the normal HUD layout.</p>
 */
public class BeghouledUpgradePanel extends Table {

    // ── Upgrade catalogue (must mirror BeghouledService.UPGRADES) ────────────
    // key = current type (lower-case), value = {nextForm, cost}
    private static final Map<String, UpgradeInfo> UPGRADES = new LinkedHashMap<>();
    static {
        UPGRADES.put("peashooter",   new UpgradeInfo("Repeater",      500));
        UPGRADES.put("repeater",     new UpgradeInfo("Gatling Pea",  1500));
        UPGRADES.put("wall-nut",     new UpgradeInfo("Tall-nut",      500));
        UPGRADES.put("puff-shroom",  new UpgradeInfo("Fume-shroom",   250));
        UPGRADES.put("cabbage-pult", new UpgradeInfo("Melon-pult",   1000));
        UPGRADES.put("melon-pult",   new UpgradeInfo("Winter Melon",  750));
    }

    private record UpgradeInfo(String displayName, int cost) {}

    // ── State ─────────────────────────────────────────────────────────────────
    private final Skin skin;
    private final GameplayController controller;

    /** Tracks which plant-type buttons are currently rendered so we only
     *  rebuild when the set changes, not every frame. */
    private final List<String> renderedTypes = new ArrayList<>();

    public BeghouledUpgradePanel(Skin skin, GameplayController controller) {
        this.skin       = skin;
        this.controller = controller;
        setTouchable(Touchable.childrenOnly);
        top().right();
        setVisible(false);   // hidden until confirmed Beghouled
    }

    // ── act: refresh button list & affordability every frame ──────────────────

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (session == null || session.getLevel() == null) {
            setVisible(false);
            return;
        }

        LevelBehavior behavior = session.getLevel().getBehavior();
        if (!(behavior instanceof BeghouledBehavior beg)) {
            setVisible(false);
            return;
        }

        setVisible(true);

        // Determine which upgradeable plant types are currently on the board.
        List<String> liveTypes = liveUpgradeableTypes(beg);

        // Rebuild buttons only when the set changes.
        if (!liveTypes.equals(renderedTypes)) {
            rebuildButtons(liveTypes);
            renderedTypes.clear();
            renderedTypes.addAll(liveTypes);
        }

        // Every frame: update button label colours to reflect affordability.
        int sun = session.getSunCount();
        for (Actor child : getChildren()) {
            if (!(child instanceof UpgradeButton btn)) continue;
            btn.refreshAffordability(sun);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<String> liveUpgradeableTypes(BeghouledBehavior beg) {
        List<String> result = new ArrayList<>();
        // getActivePlantTypes() returns a map whose values are plant-type strings
        for (String type : beg.getActivePlantTypes().values()) {
            String key = type.toLowerCase();
            if (UPGRADES.containsKey(key) && !result.contains(key)) {
                result.add(key);
            }
        }
        result.sort(String::compareTo);   // stable ordering
        return result;
    }

    // Mirrors SeedBankHud.SLOT_W / SLOT_H so buttons are identical in footprint.
    private static final int SLOT_W = 64;
    private static final int SLOT_H = 82;

    private void rebuildButtons(List<String> types) {
        clearChildren();
        pad(0f);

        if (types.isEmpty()) return;

        // Lay buttons out in a horizontal row, same height as the seed bank.
        for (String type : types) {
            UpgradeInfo info = UPGRADES.get(type);
            if (info == null) continue;

            UpgradeButton btn = new UpgradeButton(skin, type, info, controller);
            add(btn).size(SLOT_W, SLOT_H).pad(2f);
        }
    }

    // =========================================================================
    // Inner: single upgrade button
    // =========================================================================
    private static class UpgradeButton extends Table {

        private static final Color COLOR_AFFORDABLE   = new Color(0.15f, 0.55f, 0.15f, 0.92f);
        private static final Color COLOR_UNAFFORDABLE = new Color(0.35f, 0.35f, 0.35f, 0.80f);
        private static final Color COLOR_BORDER       = new Color(0f, 0f, 0f, 0.85f);
        private static final float BORDER             = 2f;

        private final UpgradeInfo   info;
        private final Label         nameLabel;
        private final Label         costLabel;
        private boolean             canAfford = true;

        UpgradeButton(Skin skin, String plantType, UpgradeInfo info,
                      GameplayController controller) {
            this.info      = info;

            setTouchable(Touchable.enabled);

            // Top label: abbreviated current → next, font scaled down to match cost label in SeedPacketWidget
            String displayCurrent = capitalize(plantType);
            nameLabel = new Label(displayCurrent + "\n→ " + info.displayName(), skin, "default");
            nameLabel.setFontScale(0.52f);
            nameLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            nameLabel.setWrap(false);
            nameLabel.setColor(Color.WHITE);

            // Bottom label: "☀ 500" — same scale as SeedPacketWidget cost label (0.62f)
            costLabel = new Label("\u2600 " + info.cost(), skin, "default");
            costLabel.setFontScale(0.62f);
            costLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            costLabel.setColor(new Color(1f, 0.95f, 0.3f, 1f));

            pad(4f, 2f, 3f, 2f);
            add(nameLabel).growX().center().row();
            add(costLabel).growX().bottom().padTop(2f);

            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    controller.upgradeBeghouledPlant(plantType);
                }
            });
        }

        void refreshAffordability(int sun) {
            boolean nowAffordable = sun >= info.cost();
            if (nowAffordable != canAfford) {
                canAfford = nowAffordable;
                invalidate();   // trigger redraw
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float x = getX(), y = getY(), w = getWidth(), h = getHeight();

            // Border
            Color old = batch.getColor().cpy();
            batch.setColor(COLOR_BORDER.r, COLOR_BORDER.g, COLOR_BORDER.b,
                    COLOR_BORDER.a * parentAlpha);
            batch.draw(WhitePixel.get(), x - BORDER, y - BORDER,
                    w + BORDER * 2f, h + BORDER * 2f);

            // Background fill
            Color bg = canAfford ? COLOR_AFFORDABLE : COLOR_UNAFFORDABLE;
            batch.setColor(bg.r, bg.g, bg.b, bg.a * parentAlpha);
            batch.draw(WhitePixel.get(), x, y, w, h);

            batch.setColor(old);

            // Labels
            nameLabel.setColor(canAfford ? Color.WHITE : new Color(0.7f, 0.7f, 0.7f, 1f));
            super.draw(batch, parentAlpha);
        }

        private static String capitalize(String s) {
            if (s == null || s.isEmpty()) return s;
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }
}