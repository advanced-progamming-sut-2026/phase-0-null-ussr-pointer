package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.IceBlock;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.board.structures.OctopusWrap;
import com.ussr.pvz.model.board.structures.PushableStructure;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.armor.ArmorType;
import com.ussr.pvz.model.entities.zombies.projectiles.*;
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

    // Sub-groups — added to this Group in draw order (back → front)
    /** Plants — rendered first (furthest back among entities) */
    private final Group plantGroup = new Group();
    /**
     * On-plant overlays: OctopusWrap, IceBlock.
     * Sit on top of the plant they cover, below zombies.
     */
    private final Group overlayGroup = new Group();
    /**
     * Plant projectiles (ProjectilePamActor).
     * In front of plants and overlays, behind zombies.
     */
    private final Group plantProjectileGroup = new Group();
    /**
     * Zombies + PushableStructures + LawnMowers, Y-sorted together.
     * Zombie PAM projectiles also land here so they sort with zombies.
     */
    private final Group zombieGroup = new Group();

    // Entity → actor maps
    private final Map<Plant, PamActor> plantActors = new HashMap<>();
    private final Map<Object, PamActor> overlayActors = new HashMap<>(); // IceBlock, OctopusWrap keyed by structure
    private final Map<Projectile, ProjectilePamActor> projectileActors = new HashMap<>();
    private final Map<Object, PamActor> zombieGroupActors = new HashMap<>(); // Zombie, LawnMower, PushableStructure
    private final Map<ZombieProjectile, PamActor> zombieProjActors = new HashMap<>();

    // Pending immediate draw calls (atlas textures, not actors)
    private final List<ZombieAtlasDrawCall> pendingZombieAtlasDraws = new ArrayList<>();

    private record ZombieAtlasDrawCall(TextureRegion region, float x, float y) {}

    // Newspaper state machine
    private enum NewspaperPhase { HAS_PAPER, DEFEAT_PLAYING, GONE }

    private final Map<Zombie, NewspaperPhase> newspaperPhase = new HashMap<>();

    // -------------------------------------------------------------------------
    // Danger-flicker: per-zombie time accumulator for the sine wave
    // -------------------------------------------------------------------------
    /**
     * Tracks how long each zombie has been in the danger zone so the sine wave
     * runs continuously rather than resetting every frame.
     */
    private final Map<Zombie, Float> dangerTime = new HashMap<>();

    /**
     * Zombie X position (in grid units) at which the danger overlay starts fading in.
     * Tune this to taste — 1.5 means "within 1.5 cells of the left edge".
     */
    private static final float DANGER_THRESHOLD_X = 1.5f;

    /** Speed of the red flicker sine wave (radians per second). Slower = more menacing. */
    private static final float DANGER_FLICKER_SPEED = 2.0f;

    // Constructor
    public EntityRenderLayer(PamPlayer pamPlayer, TextureBank textures) {
        this.pamPlayer = pamPlayer;
        this.textures = textures;
        setTouchable(Touchable.disabled);

        // Order matters — each group draws on top of the previous
        plantGroup.setTouchable(Touchable.disabled);
        overlayGroup.setTouchable(Touchable.disabled);
        plantProjectileGroup.setTouchable(Touchable.disabled);
        zombieGroup.setTouchable(Touchable.disabled);

        addActor(plantGroup);
        addActor(overlayGroup);
        addActor(plantProjectileGroup);
        addActor(zombieGroup);
    }

    // act — sync every category
    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (session == null) return;

        pendingZombieAtlasDraws.clear();

        Set<Plant> livePlants = new HashSet<>();
        Set<Object> liveOverlays = new HashSet<>();
        Set<Object> liveZombieGroup = new HashSet<>();

        syncLawnMowers(session, liveZombieGroup);
        syncPlants(session, livePlants);
        syncOverlays(session, liveOverlays);
        syncZombies(session, liveZombieGroup, delta);
        syncPushableStructures(session, liveZombieGroup);
        syncPlantProjectiles(session);
        syncZombieProjectiles(session);

        // Cleanup stale actors
        cleanupMap(plantActors, livePlants, plantGroup);
        cleanupMap(overlayActors, liveOverlays, overlayGroup);
        cleanupMap(zombieGroupActors, liveZombieGroup, zombieGroup);
        cleanupZombieProjActors(session);

        // Also clean newspaper state and danger timers for dead zombies
        newspaperPhase.keySet().removeIf(z -> !session.getZombies().contains(z));
        dangerTime.keySet().removeIf(z -> !session.getZombies().contains(z));

        // Y-sort each group independently
        sortByY(plantGroup);
        sortByY(overlayGroup);
        sortByY(zombieGroup);
    }

    // =========================================================================
    // Sync methods
    // =========================================================================

    // ---- LawnMowers ---------------------------------------------------------
    private void syncLawnMowers(GameSession session, Set<Object> live) {
        for (LawnMower mower : session.getLawnMowers()) {
            if (!mower.isAlive()) continue;
            live.add(mower);

            PamActor actor = zombieGroupActors.computeIfAbsent(mower, m -> {
                String pamPath = App.getLevelManager().getCurrentChapter().getMowerPam();
                PamActor pa = new PamActor(pamPlayer, pamPath, "idle");
                pa.setPamScale(0.45f);
                zombieGroup.addActor(pa);
                return pa;
            });

            actor.setClip(mower.isActivated() ? "attack" : "idle");
            actor.setPosition(
                    LawnGridLayout.worldX(mower.getPosition().x())
                            + LawnGridLayout.CELL_WIDTH / 2f
                            + LawnGridLayout.MOWER_DRAW_OFFSET_X,
                    LawnGridLayout.worldY(mower.getPosition().y())
                            + LawnGridLayout.MOWER_DRAW_OFFSET_Y
            );
        }
    }

    // ---- Plants -------------------------------------------------------------
    private void syncPlants(GameSession session, Set<Plant> live) {
        for (Plant plant : session.getPlants()) {
            if (!plant.isAlive() && plant.getState() != Plant.PlantState.DYING) continue;
            live.add(plant);

            PamActor actor = plantActors.computeIfAbsent(plant, p -> {
                PlantPamActor pa = new PlantPamActor(pamPlayer, plant.getPamPath(), plant.getAnimationClip());
                plantGroup.addActor(pa);
                return pa;
            });

            actor.setClip(plant.getAnimationClip());
            actor.setPosition(
                    LawnGridLayout.cellX(plant.getLocation().x())
                            + LawnGridLayout.CELL_WIDTH / 2f
                            + LawnGridLayout.PLANT_DRAW_OFFSET_X,
                    LawnGridLayout.cellY(plant.getLocation().y())
                            + LawnGridLayout.PLANT_DRAW_OFFSET_Y
            );
        }
    }

    // ---- On-plant overlays: IceBlock, OctopusWrap ---------------------------
    private void syncOverlays(GameSession session, Set<Object> live) {
        if (session.getLawn() == null) return;

        for (int row = 0; row < session.getLawn().getRows(); row++) {
            for (int col = 0; col < session.getLawn().getCols(); col++) {
                var cell = session.getLawn().getCell(row, col);
                if (cell == null) continue;
                var s = cell.getInteractableStructure();
                if (s == null || !s.isAlive()) continue;

                if (s instanceof IceBlock ice) {
                    live.add(ice);
                    PamActor actor = overlayActors.computeIfAbsent(ice, k -> {
                        PamActor pa = new PamActor(pamPlayer, ice.getPamLocation(), "freeze_idle");
                        pa.setPamScale(0.55f);
                        pa.setOffsetY(-20f);
                        overlayGroup.addActor(pa);
                        return pa;
                    });
                    positionLikePlant(actor, col, row);

                } else if (s instanceof OctopusWrap wrap) {
                    live.add(wrap);
                    PamActor actor = overlayActors.computeIfAbsent(wrap, k -> {
                        PamActor pa = new PamActor(pamPlayer, wrap.getPamLocation(), "animation3");
                        pa.setPamScale(0.55f);
                        overlayGroup.addActor(pa);
                        return pa;
                    });
                    positionLikePlant(actor, col, row);
                }
            }
        }
    }

    private void positionLikePlant(PamActor actor, int column, int row) {
        actor.setPosition(
                LawnGridLayout.cellX(column)
                        + LawnGridLayout.CELL_WIDTH / 2f
                        + LawnGridLayout.PLANT_DRAW_OFFSET_X,
                LawnGridLayout.cellY(row)
                        + LawnGridLayout.PLANT_DRAW_OFFSET_Y
        );
    }

    // ---- Zombies ------------------------------------------------------------
    /**
     * @param delta frame time — needed to advance the danger-flicker accumulator.
     */
    private void syncZombies(GameSession session, Set<Object> live, float delta) {
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.isAlive() && zombie.isDeathAnimDone()) continue;
            live.add(zombie);

            PamActor actor = zombieGroupActors.computeIfAbsent(zombie, z -> {
                String animation = resolveZombieClip(zombie);
                ZombiePamActor za = new ZombiePamActor(pamPlayer, zombie.getPamPath(), animation);
                zombieGroup.addActor(za);
                return za;
            });

            String currentClip = resolveZombieCurrentClip(zombie, actor);

            if (actor instanceof ZombiePamActor zombieActor) {
                if (!zombieActor.isPlayingSpecial()) {
                    String animEvent = zombie.pollAnimEvent();
                    if (animEvent != null) {
                        zombieActor.playOnce(animEvent, currentClip);
                    } else {
                        zombieActor.setClip(currentClip);
                    }
                }

                if (zombie.getState() == com.ussr.pvz.model.entities.zombies.ZombieActivity.DEAD) {
                    zombieActor.setClip(currentClip);
                }

                zombieActor.setArmor(zombie.getArmor());

                // --- Glow effect -------------------------------------------
                // Stay glowing until the zombie actually dies (death anim may
                // still be playing after isAlive goes false).
                zombieActor.setGlowing(zombie.isGlowing() && zombie.isAlive());

                // --- Danger flicker -----------------------------------------
                float zombieX = (float) zombie.getPosition().x();
                if (zombieX <= DANGER_THRESHOLD_X && zombie.isAlive()) {
                    // Advance this zombie's personal sine-wave clock
                    float t = dangerTime.getOrDefault(zombie, 0f) + delta;
                    dangerTime.put(zombie, t);

                    // Map proximity to [0..1]: 0 at threshold, 1 at x=0
                    float proximity = 1f - (zombieX / DANGER_THRESHOLD_X);
                    proximity = Math.max(0f, Math.min(1f, proximity));

                    // Sine flicker — always positive, scaled by proximity
                    float flicker = (float) ((Math.sin(t * DANGER_FLICKER_SPEED) + 1.0) * 0.5);
                    // Min alpha of 0.08 so the overlay never fully disappears
                    float alpha = proximity * (0.08f + 0.30f * flicker);

                    zombieActor.setDangerAlpha(alpha);
                } else {
                    // Out of danger zone — clear the overlay and the timer
                    dangerTime.remove(zombie);
                    zombieActor.setDangerAlpha(0f);
                }

            } else {
                actor.setClip(currentClip);
            }

            actor.setPosition(
                    LawnGridLayout.worldX(zombie.getPosition().x())
                            + LawnGridLayout.CELL_WIDTH / 2f
                            + LawnGridLayout.ZOMBIE_DRAW_OFFSET_X,
                    LawnGridLayout.worldY(zombie.getPosition().y())
                            + LawnGridLayout.ZOMBIE_DRAW_OFFSET_Y
            );
        }
    }

    private String resolveZombieClip(Zombie zombie) {
        return switch (zombie.getState()) {
            case WALKING -> "walk";
            case DEAD -> "die";
            case EATING -> "eat";
            case null -> "idle";
        };
    }

    private String resolveZombieCurrentClip(Zombie zombie, PamActor actor) {
        Armor armor = zombie.getArmor();
        boolean hasNewspaper = armor != null
                && armor.getArmorType() == ArmorType.NEWSPAPER
                && !armor.isDestroyed();
        boolean hadNewspaper = armor != null
                && armor.getArmorType() == ArmorType.NEWSPAPER;

        if (!hadNewspaper) {
            return resolveZombieClip(zombie);
        }

        NewspaperPhase phase = newspaperPhase.computeIfAbsent(zombie, z -> NewspaperPhase.HAS_PAPER);

        // Transition: newspaper just destroyed
        if (phase == NewspaperPhase.HAS_PAPER && !hasNewspaper) {
            newspaperPhase.put(zombie, NewspaperPhase.DEFEAT_PLAYING);
            return "newspaper_defeat";
        }

        return switch (phase) {
            case DEFEAT_PLAYING -> {
                if (actor instanceof ZombiePamActor za && !za.isPlaying()) {
                    newspaperPhase.put(zombie, NewspaperPhase.GONE);
                }
                yield "newspaper_defeat";
            }
            case HAS_PAPER -> switch (zombie.getState()) {
                case EATING -> "eat_newspaper";
                case WALKING -> "walk_newspaper";
                case DEAD -> "die";
                case null -> "idle";
            };
            case GONE -> resolveZombieClip(zombie);
        };
    }

    // ---- PushableStructures (sort with zombies by Y) ------------------------
    private void syncPushableStructures(GameSession session, Set<Object> live) {
        for (var s : session.getLawn().getAllInteractable()) {
            if (!(s instanceof PushableStructure pushable)) continue;
            if (!pushable.isAlive()) continue;
            live.add(pushable);

            PamActor actor = zombieGroupActors.computeIfAbsent(pushable, k -> {
                PamActor pa = new PamActor(pamPlayer, pushable.getType().getPamLocation(), "idle");
                pa.setPamScale(0.6f);
                pa.setOffsetY(-20f);
                zombieGroup.addActor(pa);
                return pa;
            });

            actor.setPosition(
                    LawnGridLayout.worldX(pushable.getPosition().x())
                            + LawnGridLayout.CELL_WIDTH / 2f
                            + LawnGridLayout.PLANT_DRAW_OFFSET_X,
                    LawnGridLayout.worldY(pushable.getPosition().y())
                            + LawnGridLayout.PLANT_DRAW_OFFSET_Y
            );
        }
    }


    // ---- Plant projectiles --------------------------------------------------
    private void syncPlantProjectiles(GameSession session) {
        List<Projectile> liveProjectiles = new ArrayList<>(session.getProjectiles());
        Set<Projectile> liveSet = new HashSet<>(liveProjectiles);

        for (Projectile proj : liveProjectiles) {
            ProjectilePamActor actor = projectileActors.computeIfAbsent(proj, p -> {
                Plant user = p.getUser();
                String projPam = user != null ? user.getProjectilePam() : null;
                String hitPam = user != null ? user.getHitPam() : null;
                ProjectilePamActor pa = new ProjectilePamActor(pamPlayer, projPam, hitPam);
                plantProjectileGroup.addActor(pa);
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

        // Sweep orphaned plant projectile actors
        Iterator<Map.Entry<Projectile, ProjectilePamActor>> it = projectileActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Projectile, ProjectilePamActor> entry = it.next();
            ProjectilePamActor actor = entry.getValue();
            if (!liveSet.contains(entry.getKey())) {
                if (actor.phase == ProjectilePamActor.Phase.FLYING) {
                    Projectile proj = entry.getKey();
                    float hx = LawnGridLayout.worldX(proj.getPosition().x()) + LawnGridLayout.CELL_WIDTH / 2f;
                    float hy = LawnGridLayout.worldY(proj.getPosition().y());
                    actor.triggerHit(hx, hy);
                } else if (actor.isDone()) {
                    actor.remove();
                    it.remove();
                }
            }
        }
    }

    // ---- Zombie projectiles -------------------------------------------------
    private void syncZombieProjectiles(GameSession session) {
        List<ZombieProjectile> live = new ArrayList<>(session.getZombieProjectiles());
        Set<ZombieProjectile> liveSet = new HashSet<>(live);

        for (ZombieProjectile proj : live) {
            if (!proj.isAlive()) continue;

            float screenX = LawnGridLayout.worldX((float) proj.getPosition().x())
                    + LawnGridLayout.CELL_WIDTH / 2f;
            float screenY = LawnGridLayout.worldY((float) proj.getPosition().y());

            // BoneProjectile is atlas-only
            if (proj instanceof BoneProjectile) {
                TextureRegion region = textures.region("IMAGE_ZOMBIE_BONE_PROJECTILE");
                if (region != null) {
                    pendingZombieAtlasDraws.add(new ZombieAtlasDrawCall(
                            region,
                            screenX - region.getRegionWidth() / 2f,
                            screenY
                    ));
                }
                continue;
            }

            // PAM zombie projectiles go in zombieGroup so they Y-sort with zombies
            String clip = resolveZombieProjectileClip(proj);
            PamActor actor = zombieProjActors.computeIfAbsent(proj, p -> {
                PamActor a = new PamActor(pamPlayer, p.getPamLocation(), clip);
                a.setPamScale(pamScaleForZombieProjectile(p));
                zombieGroup.addActor(a);
                return a;
            });
            actor.setPosition(screenX - actor.getWidth() / 2f, screenY);
        }

        cleanupZombieProjActors(liveSet);
    }

    private void cleanupZombieProjActors(GameSession session) {
        // overload used by act() cleanup pass
        Set<ZombieProjectile> liveSet = new HashSet<>(session.getZombieProjectiles());
        cleanupZombieProjActors(liveSet);
    }

    private void cleanupZombieProjActors(Set<ZombieProjectile> liveSet) {
        Iterator<Map.Entry<ZombieProjectile, PamActor>> it = zombieProjActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ZombieProjectile, PamActor> entry = it.next();
            if (!liveSet.contains(entry.getKey()) || !entry.getKey().isAlive()) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private String resolveZombieProjectileClip(ZombieProjectile proj) {
        if (proj instanceof ZombieBossProjectile) return "missile";
        if (proj instanceof OctopusProjectile) return "animation2";
        return "animation";
    }

    private float pamScaleForZombieProjectile(ZombieProjectile proj) {
        if (proj instanceof ZombieBossProjectile) return 0.55f;
        if (proj instanceof GargantuarImpProjectile) return 0.6f;
        return 0.45f;
    }

    // =========================================================================
    // draw
    // =========================================================================
    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Draws sub-groups in the order they were added:
        // plantGroup → overlayGroup → plantProjectileGroup → zombieGroup
        super.draw(batch, parentAlpha);

        // Atlas zombie projectiles (BoneProjectile etc.)
        for (ZombieAtlasDrawCall call : pendingZombieAtlasDraws) {
            batch.draw(call.region(), call.x(), call.y());
        }

        GameSession session = App.getGameSession();
        if (session != null) {
            renderDrops(batch, session);
        }
    }

    private void renderDrops(Batch batch, GameSession session) {
        // Non-sun collectable items (COIN, DIAMOND, PLANT_FOOD, SEED_PACK) are
        // now rendered by ItemRenderLayer using proper PAM actors.
        // Nothing to draw here.
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private static void sortByY(Group group) {
        group.getChildren().sort((a, b) -> Float.compare(b.getY(), a.getY()));
    }

    private static <K> void cleanupMap(Map<K, PamActor> map, Set<K> live, Group group) {
        Iterator<Map.Entry<K, PamActor>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, PamActor> entry = it.next();
            if (!live.contains(entry.getKey())) {
                entry.getValue().remove(); // removes from its parent group
                it.remove();
            }
        }
    }
}