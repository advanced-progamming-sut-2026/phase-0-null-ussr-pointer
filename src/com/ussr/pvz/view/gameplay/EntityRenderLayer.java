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
import com.ussr.pvz.view.animation.ProjectilePamActor;
import com.ussr.pvz.view.animation.ZombiePamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.*;

public class EntityRenderLayer extends Group {
    private final PamPlayer pamPlayer;
    private final TextureBank textures;

    // Tracks logical entities to their visual PamActors
    private final Map<Object, PamActor> entityActors = new HashMap<>();
    // Add this field at the top with the other fields
    private final Map<Projectile, ProjectilePamActor> projectileActors = new HashMap<>();
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
        // 1. Sync LawnMowers
        for (LawnMower mower : session.getLawnMowers()) {
            if (!mower.isAlive()) continue;
            entitiesThisFrame.put(mower, true);

            PamActor actor = entityActors.computeIfAbsent(mower, m -> {
                String pamPath = App.getLevelManager().getCurrentChapter().getMowerPam();
                PamActor pa = new PamActor(pamPlayer, pamPath, "idle");
                pa.setPamScale(0.45f);
                addActor(pa);
                return pa;
            });

            // Dynamically set the clip based on the mower's current state.
            // Note: Replace 'isTriggered()' with whatever method your LawnMower class uses!
            String currentClip = mower.isActivated() ? "attack" : "idle";
            actor.setClip(currentClip);

            actor.setPosition(
                    LawnGridLayout.worldX(
                            mower.getPosition().x()
                    ) + LawnGridLayout.CELL_WIDTH / 2f
                            + LawnGridLayout.MOWER_DRAW_OFFSET_X,

                    LawnGridLayout.worldY(
                            mower.getPosition().y()
                    ) + LawnGridLayout.MOWER_DRAW_OFFSET_Y
            );
        }

        // 2. Sync Plants using your PlantPamActor
        // 2. Sync Plants using your PlantPamActor
        for (Plant plant : session.getPlants()) {
            // Keep rendering if it's dead BUT currently playing its dying/explosion animation
            if (!plant.isAlive() && plant.getState() != Plant.PlantState.DYING) continue;

            entitiesThisFrame.put(plant, true);

            PamActor actor = entityActors.computeIfAbsent(plant, p -> {
                // Initialize with the smart clip manager
                PlantPamActor pa = new PlantPamActor(pamPlayer, plant.getPamPath(), plant.getAnimationClip());
                addActor(pa);
                return pa;
            });

            // Dynamically update the clip every frame based on the state manager
            actor.setClip(plant.getAnimationClip());

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
            if (!zombie.isAlive() && zombie.isDeathAnimDone()) continue;
            entitiesThisFrame.put(zombie, true);

            PamActor actor = entityActors.computeIfAbsent(zombie, z -> {
                String animation = switch (zombie.getState()) {
                    case WALKING -> "walk";
                    case DEAD -> "die";
                    case EATING -> "eat";
                    case null -> "idle";
                };
                ZombiePamActor za = new ZombiePamActor(pamPlayer, zombie.getPamPath(), animation);
                addActor(za);
                return za;
            });

            String currentClip = switch (zombie.getState()) {
                case WALKING -> "walk";
                case DEAD -> "die";
                case EATING -> "eat";
                case null -> "idle";
            };
            actor.setClip(currentClip);

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
        // 4. Cleanup destroyed entities (skip projectiles — managed separately above)
      // 3b. Sync Projectiles
        // 3b. Sync Projectiles
        List<Projectile> liveProjectiles = new ArrayList<>(session.getProjectiles());
        Set<Projectile> liveSet = new HashSet<>(liveProjectiles);

        for (Projectile proj : liveProjectiles) {
            ProjectilePamActor actor = projectileActors.computeIfAbsent(proj, p -> {
                Plant user = p.getUser();
                String projPam = user != null ? user.getProjectilePam() : null;
                String hitPam  = user != null ? user.getHitPam()        : null;
                ProjectilePamActor pa = new ProjectilePamActor(pamPlayer, projPam, hitPam);
                addActor(pa);
                return pa;
            });

            if (actor.isDone()) {
                actor.remove();
                projectileActors.remove(proj);
                continue;
            }

            if (!proj.isAlive() && actor.phase == ProjectilePamActor.Phase.FLYING) {
                float hx = LawnGridLayout.worldX(proj.getPosition().x()) + LawnGridLayout.CELL_WIDTH / 2f;
                float hy = LawnGridLayout.worldY(proj.getPosition().y());
                actor.triggerHit(hx, hy);
            } else if (proj.isAlive()) {
                actor.setPosition(
                        LawnGridLayout.worldX(proj.getPosition().x()) + LawnGridLayout.CELL_WIDTH / 2f,
                        LawnGridLayout.worldY(proj.getPosition().y())
                );
            }
        }

// Sweep orphaned projectile actors — ones whose projectile was already removed from the session list
// but are still mid-hit or were never transitioned
        Iterator<Map.Entry<Projectile, ProjectilePamActor>> projIt = projectileActors.entrySet().iterator();
        while (projIt.hasNext()) {
            Map.Entry<Projectile, ProjectilePamActor> entry = projIt.next();
            ProjectilePamActor actor = entry.getValue();

            if (!liveSet.contains(entry.getKey())) {
                // Projectile was cleaned up from session — trigger hit if still flying, else just remove
                if (actor.phase == ProjectilePamActor.Phase.FLYING) {
                    Projectile proj = entry.getKey();
                    float hx = LawnGridLayout.worldX(proj.getPosition().x()) + LawnGridLayout.CELL_WIDTH / 2f;
                    float hy = LawnGridLayout.worldY(proj.getPosition().y());
                    actor.triggerHit(hx, hy);
                    // keep it in the map so the hit plays out next frames
                } else if (actor.isDone()) {
                    actor.remove();
                    projIt.remove();
                }
                // if phase == HIT and not done yet, leave it — it'll be caught next frame
            }
        }
        Iterator<Map.Entry<Object, PamActor>> it = entityActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, PamActor> entry = it.next();
            if (entry.getKey() instanceof Projectile) continue; // handled above
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
            renderDrops(batch, session);
        }
    }


    private void renderDrops(Batch batch, GameSession session) {
        for (GroundItem item : session.getItems()) {
            if (item.isCollected()) continue;
            if (item.getItemType() == ItemType.SUN) continue; // handled by SunRenderLayer

            float screenX = LawnGridLayout.worldX(item.getPosition().x());
            float screenY = LawnGridLayout.worldY(item.getPosition().y());

            if (item.getItemType() == ItemType.PLANT_FOOD) {
                TextureRegion region = textures.region("IMAGE_PLANTFOOD");
                if (region != null) batch.draw(region, screenX, screenY, 48f, 48f);
            }
        }
    }
}