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

    // collect radius in screen pixels — tune as needed
    private static final float COLLECT_RADIUS_PX = 55f;

    private static final float PLANT_ACTOR_HALF_WIDTH = 40f;
    private static final float PLANT_ACTOR_VISUAL_CENTER_Y = 20f; // 40 (half height) - 20 (offsetY)

    /** How high above the plant's own PAM anchor the sun pops before falling to rest, in world pixels. */
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

        // mouse position in stage coords
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        getStage().getViewport().unproject(mouse);

        // sync actors with live sun items
        for (GroundItem item : session.getItems()) {
            if (item.getItemType() != ItemType.SUN) continue;
            if (item.isCollected() || !item.isAlive()) continue;

            SunActor actor = sunActors.computeIfAbsent(item, i -> {
                SunActor sa = new SunActor(pamPlayer, i);
                addActor(sa);
                return sa;
            });

            // position the actor
            float screenX, screenY;
            if (item instanceof SunToken token) {
                // during fall: interpolate Y from top of screen down to target row
                float targetScreenY = LawnGridLayout.worldY(token.getTargetRow());
                float targetScreenX = LawnGridLayout.worldX(token.getTargetCol())
                        + LawnGridLayout.CELL_WIDTH / 2f;

                if (token.isFalling()) {
                    float progress = Math.min(1f,
                            actor.getStateTime() / (float) SunToken.getFallDurationSeconds());
                    float startY = getStage().getViewport().getWorldHeight() + 40f;
                    screenY = startY + (targetScreenY - startY) * progress;
                } else {
                    screenY = targetScreenY;
                }
                screenX = targetScreenX;
            } else if (item instanceof ProducedSun sun) {
                float plantCenterX = LawnGridLayout.cellX((int) item.getPosition().x())
                        + LawnGridLayout.CELL_WIDTH / 2f
                        + LawnGridLayout.PLANT_DRAW_OFFSET_X
                        + PLANT_ACTOR_HALF_WIDTH;
                float plantCenterY = LawnGridLayout.cellY((int) item.getPosition().y())
                        + LawnGridLayout.PLANT_DRAW_OFFSET_Y
                        + PLANT_ACTOR_VISUAL_CENTER_Y;

                float restX = plantCenterX + sun.getOffsetX();
                float restY = plantCenterY + sun.getOffsetY();

                if (sun.isPopping()) {
                    float startX = plantCenterX;
                    float startY = plantCenterY;

                    float t = Math.min(1f,
                            actor.getStateTime() / ProducedSun.getPopDurationSeconds());
                    float peakY = Math.max(startY, restY) + SUN_POP_HEIGHT_PX;
                    float arc = 4f * t * (1f - t); // 0 -> 1 -> 0 across the pop
                    screenX = startX + (restX - startX) * t;
                    screenY = startY + (restY - startY) * t + arc * (peakY - Math.max(startY, restY));
                } else {
                    screenX = restX;
                    screenY = restY;
                }
            } else {
                // Fallback for any other fixed-position sun item.
                screenX = LawnGridLayout.worldX(item.getPosition().x())
                        + LawnGridLayout.CELL_WIDTH / 2f;
                screenY = LawnGridLayout.worldY(item.getPosition().y());
            }

            actor.setPosition(
                    screenX - actor.getWidth()  / 2f,
                    screenY - actor.getHeight() / 2f
            );

            // collection logic
            if (actor.isDone()) continue;

            boolean hovered = mouse.dst(screenX, screenY) < COLLECT_RADIUS_PX;
            boolean collect = collectAll || hovered;

            if (collect) {
                boolean explode = item instanceof SunToken token2
                        && token2.getDropType() == SunDropType.RADIOACTIVE
                        && token2.isFalling();

                actor.onCollected(explode);
                item.collect(); // applies reward to session
            }
        }

        // cleanup done actors
        Iterator<Map.Entry<GroundItem, SunActor>> it = sunActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<GroundItem, SunActor> entry = it.next();
            SunActor actor = entry.getValue();
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