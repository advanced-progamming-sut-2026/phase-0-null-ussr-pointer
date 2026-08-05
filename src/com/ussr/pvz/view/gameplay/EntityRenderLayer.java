package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.view.animation.PamActor;
import com.ussr.pvz.view.animation.PlantPamActor;
import com.ussr.pvz.view.animation.ZombiePamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EntityRenderLayer extends Group {
    private final PamPlayer pamPlayer;
    private final TextureBank textures;

    // Tracks logical entities to their visual PamActors
    private final Map<Object, PamActor> entityActors = new HashMap<>();

    public EntityRenderLayer(PamPlayer pamPlayer, TextureBank textures) {
        this.pamPlayer = pamPlayer;
        this.textures = textures;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        GameSession session = App.getGameSession();
        if (session == null) return;

        Map<Object, Boolean> entitiesThisFrame = new HashMap<>();

        // 1. Sync LawnMowers
        for (LawnMower mower : session.getLawnMowers()) {
            if (!mower.isAlive()) continue;
            entitiesThisFrame.put(mower, true);

            PamActor actor = entityActors.computeIfAbsent(mower, m -> {
                String pamPath = App.getLevelManager().getCurrentChapter().getMowerPam();
                // Fixed: Now uses the exact 3-argument constructor defined in your PamActor
                PamActor pa = new PamActor(pamPlayer, pamPath, "idle");
                pa.setPamScale(0.45f);
                addActor(pa);
                return pa;
            });

            actor.setPosition(
                    LawnGridLayout.worldX(
                            mower.getPosition().x()
                    ) + LawnGridLayout.CELL_WIDTH / 2f - 60f,

                    LawnGridLayout.worldY(
                            mower.getPosition().y()
                    )
            );
        }

        // 2. Sync Plants using your PlantPamActor
        for (Plant plant : session.getPlants()) {
            if (!plant.isAlive()) continue;
            entitiesThisFrame.put(plant, true);

            PamActor actor = entityActors.computeIfAbsent(plant, p -> {
                String initialClip = plant.getPlantFoodTimer() > 0 ? "plantfood" : "idle";
                PlantPamActor pa = new PlantPamActor(pamPlayer, plant.getPamPath(), initialClip);
                addActor(pa);
                return pa;
            });

            actor.setClip(plant.getPlantFoodTimer() > 0 ? "plantfood" : "idle");
            actor.setPosition(
                    LawnGridLayout.cellX(
                            plant.getLocation().x()
                    ) + LawnGridLayout.CELL_WIDTH / 2f
                            + LawnGridLayout.PLANT_DRAW_OFFSET_X,

                    LawnGridLayout.cellY(
                            plant.getLocation().y()
                    ) + LawnGridLayout.PLANT_DRAW_OFFSET_Y
            );
        }

        // 3. Sync Zombies using your ZombiePamActor
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive()) continue;
            entitiesThisFrame.put(zombie, true);

            PamActor actor = entityActors.computeIfAbsent(zombie, z -> {
                ZombiePamActor za = new ZombiePamActor(pamPlayer, zombie.getPamPath(), "walk");
                addActor(za);
                return za;
            });

            // Note: Since PamActor doesn't support the visibilityMap, armor layers won't toggle dynamically yet.
            actor.setPosition(
                    LawnGridLayout.worldX(
                            zombie.getPosition().x()
                    ) + LawnGridLayout.CELL_WIDTH / 2f
                            + LawnGridLayout.ZOMBIE_DRAW_OFFSET_X,

                    LawnGridLayout.worldY(
                            zombie.getPosition().y()
                    ) + LawnGridLayout.ZOMBIE_DRAW_OFFSET_Y
            );
        }

        // 4. Cleanup destroyed entities
        Iterator<Map.Entry<Object, PamActor>> it = entityActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, PamActor> entry = it.next();
            if (!entitiesThisFrame.containsKey(entry.getKey())) {
                entry.getValue().remove();
                it.remove();
            }
        }

        // 5. Painter's Algorithm (Y-based Z-Sorting)
        getChildren().sort((a1, a2) -> Float.compare(a2.getY(), a1.getY()));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        // Immediate mode for simple items/projectiles
        GameSession session = App.getGameSession();
        if (session != null) {
            renderProjectiles(batch, session);
            renderDrops(batch, session);
        }
    }

    private void renderProjectiles(Batch batch, GameSession session) {
        for (Projectile proj : session.getProjectiles()) {
            if (!proj.isAlive()) continue;

            float screenX = LawnGridLayout.worldX(
                    proj.getPosition().x()
            );

            float screenY = LawnGridLayout.worldY(
                    proj.getPosition().y()
            );

            TextureRegion region = textures.region("IMAGE_EFFECTS_T_PEA_PROJECTILE_T_PEA_PROJECTILE_39X36");
            if (region != null) {
                batch.draw(region, screenX, screenY + 40f, 20f, 20f);
            }
        }
    }

    private void renderDrops(Batch batch, GameSession session) {
        for (GroundItem item : session.getItems()) {
            if (item.isCollected()) continue;

            float screenX = LawnGridLayout.worldX(
                    item.getPosition().x()
            );

            float screenY = LawnGridLayout.worldY(
                    item.getPosition().y()
            );

            if (item.getItemType() == ItemType.SUN) {
                try {
                    pamPlayer.draw(batch, "768/INITIAL/EFFECTS/SUN/SUN.PAM", "idle",
                            (float) session.getElapsedSeconds(), screenX, screenY, true);
                } catch (Exception ignored) {
                    TextureRegion region = textures.region("IMAGE_DANGERROOM_CARD_SUN");
                    if (region != null) batch.draw(region, screenX, screenY, 48f, 48f);
                }
            } else if (item.getItemType() == ItemType.PLANT_FOOD) {
                TextureRegion region = textures.region("IMAGE_PLANTFOOD");
                if (region != null) batch.draw(region, screenX, screenY, 48f, 48f);
            }
        }
    }
}