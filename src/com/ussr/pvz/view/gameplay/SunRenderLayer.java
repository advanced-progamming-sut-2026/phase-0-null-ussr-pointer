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
import com.ussr.pvz.model.entities.items.sun.ProducedSun;
import com.ussr.pvz.model.entities.items.sun.SunToken;
import com.ussr.pvz.model.entities.items.sun.SunDropType;
import com.ussr.pvz.view.animation.SunActor;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SunRenderLayer extends Group {

    private final PamPlayer pamPlayer;
    private final Map<GroundItem, SunActor> sunActors = new HashMap<>();

    private static final float COLLECT_RADIUS_PX = 55f;
    private static final float SUN_POP_HEIGHT_PX = 90f;

    public SunRenderLayer(PamPlayer pamPlayer) {
        this.pamPlayer = pamPlayer;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        GameSession session = App.getGameSession();
        if (session == null) return;
        boolean collectAll = Gdx.input.isKeyJustPressed(Input.Keys.A);
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        getStage().getViewport().unproject(mouse);
        for (GroundItem item : session.getItems()) {
            if (item.getItemType() != ItemType.SUN) continue;
            if (item.isCollected() || !item.isAlive()) continue;
            SunActor actor = getOrCreateActor(item);
            Vector2 screenPos = computeScreenPosition(item);
            actor.setPosition(screenPos.x - actor.getWidth() / 2f, screenPos.y - actor.getHeight() / 2f);
            if (actor.isDone()) continue;
            handleCollection(item, actor, mouse, collectAll, screenPos);
        }
        removeDeadActors();
    }

    // ── Submethod 1: actor cache ──────────────────────────────────────────────

    private SunActor getOrCreateActor(GroundItem item) {
        return sunActors.computeIfAbsent(item, i -> {
            SunActor sa = new SunActor(pamPlayer, i);
            addActor(sa);
            return sa;
        });
    }

    // ── Submethod 2: screen position ──────────────────────────────────────────

    private Vector2 computeScreenPosition(GroundItem item) {
        if (item instanceof SunToken token)       return computeSunTokenPosition(token);
        if (item instanceof ProducedSun sun)      return computeProducedSunPosition(item, sun);
        return new Vector2(
                LawnGridLayout.worldX(item.getPosition().x()) + LawnGridLayout.CELL_WIDTH / 2f,
                LawnGridLayout.worldY(item.getPosition().y())
        );
    }

    private Vector2 computeSunTokenPosition(SunToken token) {
        float screenX = LawnGridLayout.worldX(token.getTargetCol()) + LawnGridLayout.CELL_WIDTH / 2f;
        float targetScreenY = LawnGridLayout.worldY(token.getTargetRow());
        float screenY;
        if (token.isFalling()) {
            float progress = (float)(token.getCurrentY() / Math.max(token.getTargetRow(), 1));
            float startY = getStage().getViewport().getWorldHeight() + 40f;
            screenY = startY + (targetScreenY - startY) * progress;
        } else {
            screenY = targetScreenY;
        }
        return new Vector2(screenX, screenY);
    }

    private Vector2 computeProducedSunPosition(GroundItem item, ProducedSun sun) {
        float restX = LawnGridLayout.worldX(item.getPosition().x()) + LawnGridLayout.CELL_WIDTH / 2f;
        float restY = LawnGridLayout.worldY(item.getPosition().y());
        if (!sun.isPopping()) return new Vector2(restX, restY);
        float startX = LawnGridLayout.cellX((int) item.getPosition().x()) + LawnGridLayout.CELL_WIDTH / 2f
                + LawnGridLayout.PLANT_DRAW_OFFSET_X;
        float startY = LawnGridLayout.cellY((int) item.getPosition().y())
                + LawnGridLayout.PLANT_DRAW_OFFSET_Y;
        float t    = sun.getPopProgress();
        float peakY = Math.max(startY, restY) + SUN_POP_HEIGHT_PX;
        float arc   = 4f * t * (1f - t);
        return new Vector2(
                startX + (restX - startX) * t,
                startY + (restY - startY) * t + arc * (peakY - Math.max(startY, restY))
        );
    }

    // ── Submethod 3: collection ───────────────────────────────────────────────

    private void handleCollection(GroundItem item, SunActor actor,
                                  Vector2 mouse, boolean collectAll, Vector2 screenPos) {
        boolean hovered = mouse.dst(screenPos.x, screenPos.y) < COLLECT_RADIUS_PX;
        if (!collectAll && !hovered) return;
        boolean explode = item instanceof SunToken token2
                && token2.getDropType() == SunDropType.RADIOACTIVE
                && token2.isFalling();
        actor.onCollected(explode);
        item.collect();
    }

    // ── Submethod 4: cleanup ──────────────────────────────────────────────────

    private void removeDeadActors() {
        Iterator<Map.Entry<GroundItem, SunActor>> it = sunActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<GroundItem, SunActor> entry = it.next();
            SunActor  actor = entry.getValue();
            GroundItem item = entry.getKey();
            if (actor.isDone() || !item.isAlive() || item.isCollected()) {
                actor.remove();
                it.remove();
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }
}