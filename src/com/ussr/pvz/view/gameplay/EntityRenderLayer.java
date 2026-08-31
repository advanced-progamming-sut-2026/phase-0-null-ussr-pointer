package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.structures.IceBlock;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.board.structures.OctopusWrap;
import com.ussr.pvz.model.board.structures.PushableStructure;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.projectiles.BowlingNutProjectile;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.projectiles.hit.ButterHit;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieActivity;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.armor.ArmorType;
import com.ussr.pvz.model.entities.zombies.projectiles.*;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.util.Vec2;
import com.ussr.pvz.view.animation.PamActor;
import com.ussr.pvz.view.animation.PlantPamActor;
import com.ussr.pvz.view.animation.ProjectilePamActor;
import com.ussr.pvz.view.animation.ZombiePamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.*;

public class EntityRenderLayer extends Group {
    private static final String CHILL_PLANT_PAM =
            "768/FULL/EFFECTS/FROSTBITE_CHILL_PLANT/FROSTBITE_CHILL_PLANT.PAM";

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

    private final ZombossHitboxDebugOverlay zombossHitboxOverlay = new ZombossHitboxDebugOverlay();

    // Entity → actor maps
    private final Map<Plant, PamActor> plantActors = new HashMap<>();
    // Chill effects are keyed by Plant; IceBlock and OctopusWrap are keyed by structure.
    private final Map<Object, PamActor> overlayActors = new HashMap<>();
    private final Map<Projectile, ProjectilePamActor> projectileActors = new HashMap<>();
    private final Map<Object, PamActor> zombieGroupActors = new HashMap<>(); // Zombie, LawnMower, PushableStructure
    private final Map<ZombieProjectile, PamActor> zombieProjActors = new HashMap<>();
    private final Set<ZombieProjectile> zombieProjHitTriggered = new HashSet<>();

    // Last commanded render target per projectile, used so we only issue a new
    // tween when the underlying model actually advanced a physics tick, instead
    // of re-snapping the actor to the same spot every render frame.
    private final Map<Projectile, float[]> plantProjectileRenderTargets = new HashMap<>();
    private final Map<ZombieProjectile, float[]> zombieProjectileRenderTargets = new HashMap<>();
    private final Map<Zombie, float[]> zombieRenderTargets = new HashMap<>();
    private final Map<PushableStructure, float[]> pushableRenderTargets = new HashMap<>();

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

    /** How long a pushed structure (e.g. Troglobite's ice block) takes to visually slide one column. */
    private static final float PUSHABLE_SLIDE_DURATION = 0.5f;

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
        addActor(zombossHitboxOverlay);
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
        zombieRenderTargets.keySet().removeIf(z -> !session.getZombies().contains(z));
        pushableRenderTargets.keySet().removeIf(p -> !liveZombieGroup.contains(p));

        // Y-sort each group independently
        sortByY(plantGroup);
        sortByY(overlayGroup);
        sortByY(zombieGroup);
    }

    // =========================================================================
    // Sync methods
    // =========================================================================

    // ---- LawnMowers / Brains ------------------------------------------------
    private void syncLawnMowers(GameSession session, Set<Object> live) {
        for (LawnMower mower : session.getLawnMowers()) {
            if (!mower.isAlive()) continue;
            live.add(mower);

            PamActor actor = zombieGroupActors.computeIfAbsent(mower, m -> {
                com.ussr.pvz.model.level.Chapter sessionChapter = session.getLevel() == null
                        ? null
                        : App.getLevelManager().findChapter(session.getLevel().getChapter());
                String pamPath = sessionChapter != null ? sessionChapter.getMowerPam() : null;
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

            if (plant.consumeJustTransformed()) {
                PamActor stale = plantActors.remove(plant);
                if (stale != null) plantGroup.removeActor(stale);
            }

            PamActor actor = plantActors.computeIfAbsent(plant, p -> {
                PlantPamActor pa = new PlantPamActor(pamPlayer, plant.getPamPath(), plant.getAnimationClip());
                plantGroup.addActor(pa);
                return pa;
            });

            String clip = plant.getAnimationClip();
            boolean isPlantFoodIntro = "plantfood".equals(clip)
                    && plant.isPlantFoodIntroActive();
            boolean isImitateIdle = plant.getState() == Plant.PlantState.IMITATE_IDLE;
            boolean isImitateAttack = plant.getState() == Plant.PlantState.IMITATE_ATTACK;
            actor.setLooping(!isPlantFoodIntro && !isImitateIdle && !isImitateAttack);
            actor.setClip(clip);
            if (isPlantFoodIntro && !actor.isPlaying()) {
                if (plant.onPlantFoodIntroClipFinished()) {
                    actor.resetAnimation();
                }
            } else if (isImitateIdle && !actor.isPlaying()) {
                plant.onImitateIdleClipFinished();
                actor.resetAnimation();
            } else if (isImitateAttack && !actor.isPlaying()) {
                plant.onImitateAttackClipFinished();
                actor.resetAnimation();
            }
            actor.setGreyTint(plant.isImitationOverlayActive());
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

        syncChillOverlays(session, live);
        syncMissileReticles(session, live);

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

    private void syncChillOverlays(GameSession session, Set<Object> live) {
        if (session.getPlants() == null) return;

        for (Plant plant : session.getPlants()) {
            int chillLevel = plant.getChillLevel();
            if (!plant.isAlive() || plant.getLocation() == null
                    || chillLevel < 1 || chillLevel >= Plant.MAX_CHILL_LEVEL) {
                continue;
            }

            live.add(plant);
            PamActor actor = overlayActors.computeIfAbsent(plant, key -> {
                PamActor pamActor = new PamActor(
                        pamPlayer,
                        CHILL_PLANT_PAM,
                        "chill_stage1"
                );
                pamActor.setPamScale(0.55f);
                pamActor.setOffsetY(-20f);
                overlayGroup.addActor(pamActor);
                return pamActor;
            });

            actor.setClip(chillLevel == 1 ? "chill_stage1" : "chill_stage2");
            positionLikePlant(actor, plant.getLocation().x(), plant.getLocation().y());
        }
    }

    private void syncMissileReticles(GameSession session, Set<Object> live) {
        for (ZombieProjectile proj : session.getZombieProjectiles()) {
            if (!(proj instanceof MissileProjectile missile) || !missile.isAlive()) continue;
            if (missile.getPhase() == MissileProjectile.Phase.EXPLODING) continue; // gone the instant it lands

            live.add(missile);
            PamActor actor = overlayActors.computeIfAbsent(missile, k -> {
                PamActor pa = new PamActor(pamPlayer, missile.getPamLocation(), "missile_lock_reticle");
                pa.setPamScale(0.55f);
                overlayGroup.addActor(pa);
                return pa;
            });
            positionLikePlant(actor, missile.getTargetCol(), missile.getTargetRow());
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
            if (zombie.isBossMirror()) continue;
            if (!zombie.isAlive() && zombie.isDeathAnimDone()) continue;
            live.add(zombie);

            boolean isNewActor = !zombieGroupActors.containsKey(zombie);
            PamActor actor = zombieGroupActors.computeIfAbsent(zombie, key -> {
                Zombie z = (Zombie) key;
                String animation = resolveZombieClip(z);
                if (z.getZombossController() != null) {
                    animation = z.getZombossController().getPreferredClip();
                }
                // SunProducerZombie has no PAM yet — use a placeholder actor.
                if ("SunProducerZombie".equals(z.getAlias())) {
                    SunProducerPlaceholderActor ph = new SunProducerPlaceholderActor();
                    zombieGroup.addActor(ph);
                    return ph;
                }
                ZombiePamActor za = new ZombiePamActor(pamPlayer, z.getPamPath(), animation);

                if (z.getZombossController() != null) {
                    ZombossController zc = z.getZombossController();
                    za.setPamScale(zc.getDrawScale());
                    za.setOffsetX(zc.getDrawOffsetX());
                    za.setOffsetY(zc.getDrawOffsetY());
                }

                zombieGroup.addActor(za);
                return za;
            });

            String currentClip = resolveZombieCurrentClip(zombie, actor);

            if (actor instanceof ZombiePamActor zombieActor) {
                ZombossController boss = zombie.getZombossController();

                if (zombie.getState() == ZombieActivity.DEAD) {
                    if (!zombieActor.isPlayingSpecial()) {
                        List<String> deathSeq = zombie.pollAnimSequence();
                        if (deathSeq != null) {
                            zombieActor.playDeathSequence(deathSeq);
                        } else {
                            String deathClip = boss != null ? boss.resolveClip("die") : currentClip;
                            zombieActor.playDeath(deathClip);
                        }
                    }
                } else {
                    boolean bossStunned = boss != null && boss.isStunned();
                    boolean bossLocked = boss != null && boss.isMoveLocked() && boss.getLockedClip() != null;
                    String idleClip = bossStunned ? boss.getStunClip()
                            : bossLocked ? boss.getLockedClip()
                              : (boss != null ? boss.resolveClip(currentClip) : currentClip);
                    if (!zombieActor.isPlayingSpecial()) {
                        List<String> animSeq = zombie.pollAnimSequence();
                        if (animSeq != null) {
                            zombieActor.playSequence(animSeq, idleClip, false);
                        } else if (!isNewActor) {
                            // Guard against re-setting clip on initial spawn frame to prevent double intro playback
                            zombieActor.setClip(idleClip);
                        }
                    }
                }

                zombieActor.setArmor(zombie.getArmor());

                zombieActor.setFrozenSolid(zombie.isAnimationPaused());

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

                // --- Status colour overlay ----------------------------------
                zombieActor.setZombieStatus(zombie.getStatus());

                // --- Horizontal mirror (moving right = hypnotized / Prospector reverse) ---
                zombieActor.setMirroredHorizontally(
                        zombie.isAlive()
                                && zombie.getSpeed() != null
                                && zombie.getSpeed().x() > 0
                );

            } else {
                actor.setClip(currentClip);
            }

            float targetX = LawnGridLayout.worldX(zombie.getPosition().x())
                    + LawnGridLayout.CELL_WIDTH / 2f
                    + LawnGridLayout.ZOMBIE_DRAW_OFFSET_X;

            float logicalY = (float) zombie.getPosition().y();
            if ("ZombossMammoth".equals(zombie.getAlias())) {
                logicalY = (LawnGridLayout.ROWS / 2) - 0.5f;
            }
            float targetY = LawnGridLayout.worldY(logicalY)
                    + LawnGridLayout.ZOMBIE_DRAW_OFFSET_Y;

            // Interpolate position for Boss/dashing units to prevent tick-stepping jumps
            if (zombie.getZombossController() != null) {
                float[] lastTarget = zombieRenderTargets.get(zombie);
                if (lastTarget == null) {
                    actor.setPosition(targetX, targetY);
                    zombieRenderTargets.put(zombie, new float[]{targetX, targetY});
                } else if (lastTarget[0] != targetX || lastTarget[1] != targetY) {
                    actor.clearActions();
                    actor.addAction(Actions.moveTo(targetX, targetY, ActiveGameplayView.TICK_RATE));
                    zombieRenderTargets.put(zombie, new float[]{targetX, targetY});
                }
            } else {
                actor.setPosition(targetX, targetY);
            }
        }
    }

    private String resolveZombieClip(Zombie zombie) {
        return switch (zombie.getState()) {
            case WALKING -> "walk";
            case DEAD -> "die";
            case EATING -> "eat";
            case PUSHING -> "push";
            case null -> "idle";
        };
    }

    private String resolveZombieCurrentClip(Zombie zombie, PamActor actor) {
        if ("ZombieBarrelRoller".equals(zombie.getAlias())
                && (zombie.getPushedStructure() == null
                || !zombie.getPushedStructure().isAlive())) {
            return switch (zombie.getState()) {
                case WALKING -> "walk2";
                case EATING -> "eat2";
                case DEAD -> "die2";
                case PUSHING -> "walk2";
                case null -> "idle2";
            };
        }

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
                case PUSHING -> "walk_newspaper";
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

            float targetX = LawnGridLayout.worldX(pushable.getPosition().x())
                    + LawnGridLayout.CELL_WIDTH / 2f
                    + LawnGridLayout.PLANT_DRAW_OFFSET_X;
            float targetY = LawnGridLayout.worldY(pushable.getPosition().y())
                    + LawnGridLayout.PLANT_DRAW_OFFSET_Y;

            float[] lastTarget = pushableRenderTargets.get(pushable);
            if (lastTarget == null) {
                actor.setPosition(targetX, targetY);
                pushableRenderTargets.put(pushable, new float[]{targetX, targetY});
            } else if (lastTarget[0] != targetX || lastTarget[1] != targetY) {
                actor.clearActions();
                actor.addAction(Actions.moveTo(targetX, targetY, PUSHABLE_SLIDE_DURATION));
                pushableRenderTargets.put(pushable, new float[]{targetX, targetY});
            }
        }
    }

    // ---- Plant projectiles --------------------------------------------------
    private void syncPlantProjectiles(GameSession session) {
        List<Projectile> liveProjectiles = new ArrayList<>(session.getProjectiles());
        Set<Projectile> liveSet = new HashSet<>(liveProjectiles);

        for (Projectile proj : liveProjectiles) {
            ProjectilePamActor actor = projectileActors.computeIfAbsent(proj, p -> {
                String projPam;
                String hitPam;
                if (p instanceof BowlingNutProjectile nut) {
                    projPam = nut.getVisualPamPath();
                    hitPam = null;
                } else {
                    Plant user = p.getUser();
                    boolean useFoodVariant = user != null
                            && user.isBuffed()
                            && user.getPlantFoodProjectilePam() != null
                            && !user.getPlantFoodProjectilePam().isBlank();
                    boolean isButterHit = p.getHitEffectStrategy() instanceof ButterHit;
                    if (useFoodVariant) {
                        projPam = user.getPlantFoodProjectilePam();
                        String foodHitPam = user.getPlantFoodHitPam();
                        hitPam = (foodHitPam != null && !foodHitPam.isBlank())
                                ? foodHitPam : user.getHitPam();
                    } else {
                        projPam = user != null ? user.getProjectilePam() : null;
                        String butterHitPam = user != null ? user.getButterHitPam() : null;
                        hitPam = (isButterHit && butterHitPam != null && !butterHitPam.isBlank())
                                ? butterHitPam
                                : (user != null ? user.getHitPam() : null);
                    }
                }
                ProjectilePamActor pa = new ProjectilePamActor(pamPlayer, projPam, hitPam);
                if (p instanceof BowlingNutProjectile nut) {
                    pa.setPamScale(nut.getVisualScale());
                    pa.setClockwiseSpinDegPerSec(nut.getRollSpinDegPerSec());
                }

                float[] start = plantProjectileScreenPosition(p);
                float startX = start[0];
                float startY = start[1];
                pa.setPosition(startX, startY);
                plantProjectileRenderTargets.put(p, new float[]{startX, startY});

                plantProjectileGroup.addActor(pa);
                return pa;
            });

            if (actor.isDone()) {
                actor.remove();
                projectileActors.remove(proj);
                plantProjectileRenderTargets.remove(proj);
                continue;
            }

            if (!proj.isAlive() && actor.phase == ProjectilePamActor.Phase.FLYING) {
                float[] impact = plantProjectileScreenPosition(proj);
                actor.clearActions();
                actor.triggerHit(impact[0], impact[1]);
                plantProjectileRenderTargets.remove(proj);
            } else if (proj.isAlive()) {
                float[] target = plantProjectileScreenPosition(proj);
                float targetX = target[0];
                float targetY = target[1];

                float[] lastTarget = plantProjectileRenderTargets.get(proj);
                if (lastTarget == null || lastTarget[0] != targetX || lastTarget[1] != targetY) {
                    actor.clearActions();
                    actor.addAction(Actions.moveTo(targetX, targetY, ActiveGameplayView.TICK_RATE));
                    plantProjectileRenderTargets.put(proj, new float[]{targetX, targetY});
                }
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
                    float[] impact = plantProjectileScreenPosition(proj);
                    actor.clearActions();
                    actor.triggerHit(impact[0], impact[1]);
                    plantProjectileRenderTargets.remove(proj);
                } else if (actor.isDone()) {
                    actor.remove();
                    it.remove();
                    plantProjectileRenderTargets.remove(entry.getKey());
                }
            }
        }
    }

    private float[] plantProjectileScreenPosition(Projectile projectile) {
        if (projectile.getMoveStrategy() instanceof ArcMove) {
            float x = LawnGridLayout.worldX(projectile.getPosition().x())
                    + LawnGridLayout.CELL_WIDTH / 2f;
            float y = LawnGridLayout.worldY(projectile.getPosition().y())
                    + (float) projectile.getVisualHeight()
                    * LawnGridLayout.CELL_HEIGHT;
            return new float[]{x, y};
        }

        Vec2 origin = projectile.getVisualLaunchOrigin();
        float blend = projectile.getVisualLaunchBlend();

        float x = LawnGridLayout.worldX(projectile.getPosition().x())
                + LawnGridLayout.CELL_WIDTH / 2f
                + ((float) origin.x() - 0.5f) * LawnGridLayout.CELL_WIDTH * blend;
        float y = LawnGridLayout.worldY(projectile.getPosition().y())
                + (float) projectile.getVisualHeight() * LawnGridLayout.CELL_HEIGHT
                + (float) origin.y() * LawnGridLayout.CELL_HEIGHT;

        return new float[]{x, y};
    }

    // ---- Zombie projectiles -------------------------------------------------
    private void syncZombieProjectiles(GameSession session) {
        List<ZombieProjectile> live = new ArrayList<>(session.getZombieProjectiles());
        Set<ZombieProjectile> liveSet = new HashSet<>(live);

        for (ZombieProjectile proj : live) {
            if (!proj.isAlive()) {
                triggerZombieProjectileHit(proj);
                continue;
            }

            // Missile projectiles show only the ground reticle during TARGETING —
            // the missile actor itself doesn't exist yet.
            if (proj instanceof MissileProjectile missile
                    && missile.getPhase() == MissileProjectile.Phase.TARGETING) {
                continue;
            }

            float screenX = LawnGridLayout.worldX((float) proj.getPosition().x())
                    + LawnGridLayout.CELL_WIDTH / 2f;
            float screenY = LawnGridLayout.worldY((float) proj.getPosition().y())
                    + (float) proj.getVisualHeight() * LawnGridLayout.CELL_HEIGHT;

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
                float startX = screenX - a.getWidth() / 2f;
                a.setPosition(startX, screenY);
                zombieProjectileRenderTargets.put(p, new float[]{startX, screenY});
                zombieGroup.addActor(a);
                return a;
            });

            // Missiles switch clip mid-flight (falling -> exploding); baby sharks
            // switch through swim -> submerge -> attack. Keep both in sync.
            if (proj instanceof MissileProjectile || proj instanceof BabySharkProjectile) {
                actor.setClip(clip);
            }

            // Same fixed-tick-vs-render-frame mismatch as plant projectiles — tween
            // toward the new model position instead of snapping every render frame.
            float targetX = screenX - actor.getWidth() / 2f;
            float[] lastTarget = zombieProjectileRenderTargets.get(proj);
            if (lastTarget == null || lastTarget[0] != targetX || lastTarget[1] != screenY) {
                actor.clearActions();
                actor.addAction(Actions.moveTo(targetX, screenY, ActiveGameplayView.TICK_RATE));
                zombieProjectileRenderTargets.put(proj, new float[]{targetX, screenY});
            }
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
            ZombieProjectile proj = entry.getKey();
            boolean orphaned = !liveSet.contains(proj) || !proj.isAlive();
            if (!orphaned) continue;

            if (!liveSet.contains(proj)) {
                triggerZombieProjectileHit(proj);
            }

            String hitPam = proj.getHitPam();
            boolean stillPlayingHit = hitPam != null && !hitPam.isBlank()
                    && zombieProjHitTriggered.contains(proj)
                    && entry.getValue().isPlaying();
            if (stillPlayingHit) continue;

            entry.getValue().remove();
            it.remove();
            zombieProjHitTriggered.remove(proj);
            zombieProjectileRenderTargets.remove(proj);
        }
    }

    private void triggerZombieProjectileHit(ZombieProjectile proj) {
        if (zombieProjHitTriggered.contains(proj)) return;
        String hitPam = proj.getHitPam();
        if (hitPam == null || hitPam.isBlank()) return;
        PamActor actor = zombieProjActors.get(proj);
        if (actor == null) return;
        actor.switchPam(hitPam, "idle", true);
        zombieProjHitTriggered.add(proj);
    }

    private String resolveZombieProjectileClip(ZombieProjectile proj) {
        if (proj instanceof MissileProjectile missile) {
            return missile.getPhase() == MissileProjectile.Phase.EXPLODING
                    ? "missile_explosion"
                    : "missile";
        }
        if (proj instanceof BabySharkProjectile shark) {
            return switch (shark.getPhase()) {
                case IDLE -> shark.getIdleClip();
                case SWIMMING -> shark.getIdleClip();
                case SUBMERGING -> "submerge";
                case ATTACKING -> "attack";
            };
        }
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

    // =========================================================================
    // SunProducerPlaceholderActor
    // =========================================================================
    /**
     * Temporary stand-in for SunProducerZombie until its PAM file is available.
     * Draws a pulsing golden sun circle using WhitePixel so it's clearly visible
     * without any asset dependency. Swap this out by removing the
     * "SunProducerZombie" check in {@code syncZombies} once the PAM is added.
     */
    private static final class SunProducerPlaceholderActor extends PamActor {
        private static final float RADIUS   = 28f;
        private static final float PULSE    = 1.8f; // cycles per second
        private float time = 0f;

        SunProducerPlaceholderActor() {
            super(null, null, null); // PamActor with null player — no PAM drawn
            setSize(RADIUS * 2f, RADIUS * 2f);
            setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        }

        @Override
        public void act(float delta) {
            // Don't call super — no PamPlayer to tick
            time += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float pulse = 0.75f + 0.25f * (float) Math.sin(time * PULSE * 2 * Math.PI);

            float cx = getX() + RADIUS;
            float cy = getY() + RADIUS;
            float r  = RADIUS * pulse;

            // Outer glow (semi-transparent amber)
            Color old = batch.getColor().cpy();
            batch.setColor(1f, 0.80f, 0.10f, 0.40f * parentAlpha);
            batch.draw(com.ussr.pvz.view.util.WhitePixel.get(),
                    cx - r - 6f, cy - r - 6f, (r + 6f) * 2f, (r + 6f) * 2f);

            // Core (opaque yellow)
            batch.setColor(1f, 0.92f, 0.15f, 0.95f * parentAlpha);
            batch.draw(com.ussr.pvz.view.util.WhitePixel.get(),
                    cx - r, cy - r, r * 2f, r * 2f);

            // Sun symbol "☀" label — drawn as a tiny text actor if available,
            // but since we have no font here, just draw a small dark cross.
            batch.setColor(0.6f, 0.45f, 0f, 0.85f * parentAlpha);
            float arm = r * 0.35f;
            batch.draw(com.ussr.pvz.view.util.WhitePixel.get(),
                    cx - arm * 0.2f, cy - arm, arm * 0.4f, arm * 2f);
            batch.draw(com.ussr.pvz.view.util.WhitePixel.get(),
                    cx - arm, cy - arm * 0.2f, arm * 2f, arm * 0.4f);

            batch.setColor(old);
        }

        // PamActor stubs — nothing to do
        @Override public void setClip(String clip) {}
        @Override public void resetAnimation() {}
        @Override public boolean isPlaying() { return true; }
    }
}