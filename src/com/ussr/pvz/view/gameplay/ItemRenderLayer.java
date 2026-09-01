package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.view.animation.GroundItemActor;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Render layer for all non-sun collectable ground items:
 * COIN, DIAMOND, PLANT_FOOD, SEED_PACK.
 *
 * Mirrors the structure of SunRenderLayer:
 *  - one GroundItemActor per live item, created lazily
 *  - hover within COLLECT_RADIUS_PX or pressing 'A' collects everything
 *  - done actors are removed and their map entries cleaned up
 *
 * Added to the scene stack in ActiveGameplayView, above entityLayer and
 * sunLayer so items appear on top of the lawn but below the HUD.
 */
public class ItemRenderLayer extends Group {

    /** Screen-pixel radius within which hovering collects an item. */
    private static final float COLLECT_RADIUS_PX = 55f;

    private final PamPlayer pamPlayer;
    private final Map<GroundItem, GroundItemActor> itemActors = new HashMap<>();

    public ItemRenderLayer(PamPlayer pamPlayer) {
        this.pamPlayer = pamPlayer;
        setTouchable(Touchable.disabled);
    }

    // ── act ──────────────────────────────────────────────────────────────────

    @Override
    public void act(float delta) {
        super.act(delta); // advances all child actors
        GameSession session = App.getGameSession();
        if (session == null) return;
        boolean collectAll = Gdx.input.isKeyJustPressed(Input.Keys.A);
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        getStage().getViewport().unproject(mouse);
        for (GroundItem item : session.getItems()) {
            if (item.getItemType() == ItemType.SUN) continue; // SunRenderLayer owns these
            if (item.isCollected() || !item.isAlive())        continue;
            float screenX = LawnGridLayout.worldX(item.getPosition().x()) + LawnGridLayout.CELL_WIDTH / 2f;
            float screenY = LawnGridLayout.worldY(item.getPosition().y());
            GroundItemActor actor = itemActors.computeIfAbsent(item, i -> {
                GroundItemActor a = new GroundItemActor(pamPlayer, i);
                addActor(a);
                return a;
            });
            actor.setPosition(screenX - actor.getWidth()  / 2f, screenY - actor.getHeight() / 2f);
            if (actor.isDone()) continue;
            boolean hovered = mouse.dst(screenX, screenY) < COLLECT_RADIUS_PX;
            if (collectAll || hovered) {
                actor.onCollected();
            }
        }

        Iterator<Map.Entry<GroundItem, GroundItemActor>> it = itemActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<GroundItem, GroundItemActor> entry = it.next();
            GroundItemActor actor = entry.getValue();
            GroundItem      item  = entry.getKey();
            if (actor.isDone() || !item.isAlive() || item.isCollected()) {
                actor.remove();
                it.remove();
            }
        }
    }

    // ── draw ─────────────────────────────────────────────────────────────────

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }
}