package com.ussr.pvz.model.entities.zombies;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.Brain;
import com.ussr.pvz.model.board.structures.PushableStructure;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.items.PlantFoodDrop;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.attack.AttackBehavior;
import com.ussr.pvz.model.entities.zombies.defense.DefenseBehavior;
import com.ussr.pvz.model.entities.zombies.effect.EffectStatus;
import com.ussr.pvz.model.entities.zombies.move.HypnotizedMoveBehavior;
import com.ussr.pvz.model.entities.zombies.move.JumpMove;
import com.ussr.pvz.model.entities.zombies.move.MoveBehavior;
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.model.entities.zombies.zomboss.ZombossController;
import com.ussr.pvz.model.level.behavior.CouchIZombieBehavior;
import com.ussr.pvz.model.level.behavior.IZombieBehavior;
import com.ussr.pvz.model.level.behavior.MultiplayerIZombieBehavior;
import com.ussr.pvz.model.util.Vec2;

import java.util.*;

public class Zombie extends GameEntity implements Damageable {

    public static final double DEFAULT_FREEZE_DURATION = 5.0;
    public static final double DEFAULT_BUTTER_DURATION = 4.0;
    public static final double DEFAULT_POISON_DURATION = 5.0;
    public static final double DEFAULT_FIRE_DURATION = 5.0;
    public static final float FREEZE_SPEED_MULTIPLIER = 0.5f;
    public static final float BUTTER_SPEED_MULTIPLIER = 0.7f;


    private static final Random RAND = new Random();
    private final String name;

    private MoveBehavior moveBehavior;
    private EffectStatus effectStatus;
    private DefenseBehavior defenseBehavior;
    private AttackBehavior attackBehavior;
    private Armor armor;
    private PushableStructure pushedStructure;
    private int pushableRespawnsRemaining = 0;

    private int hp;
    private int maxHp;
    private double eatDps;
    private double statusTimeRemaining = 0.0;
    private double poisonTickTimer;
    private int poisonTickDamage;
    private ZombieSize size;
    private ZombieActivity state = ZombieActivity.WALKING;
    private boolean isGlowing;
    private java.util.List<String> damageWhileSubmerged;
    private java.util.List<String> damageWhileSubmergedPlantfoodOnly;
    private String pamPath;
    private boolean bossMirror = false;
    // Render-only backref to the boss controller when this zombie is a Zomboss
    // primary or mirror body. NOT set as effectStatus (that stays primary-only,
    // set in ZombossFactory) so it never gets ticked twice per frame — this is
    // purely so EntityRenderLayer can ask "is my boss stunned / what's my death clip".
    private ZombossController zombossController;
    private float deathTimer = -1f; // -1 means not dying yet
    // Some zombie PAM death clips are longer than two seconds. Keeping the
    // entity for four seconds lets both the body fall and the late head drop
    // finish before EntityRenderLayer removes its actor.
    private static final float DEATH_ANIM_DURATION = 4.0f;
    private final Queue<java.util.List<String>> pendingAnimEvents = new ArrayDeque<>();

    private static final float SLIPPERY_SLIDE_DURATION = 2.25f;

    private boolean slidingBetweenRows;
    private float slipperySlideElapsed;
    private double slipperyStartRow;
    private double slipperyTargetRow;

    public void queueAnimEvent(String clipName) {
        if (clipName != null && !clipName.isBlank()) pendingAnimEvents.offer(List.of(clipName));
    }

    public void queueAnimSequence(List<String> clips) {
        if (clips == null || clips.isEmpty()) return;
        List<String> f = new ArrayList<>();
        for (String c : clips) if (c != null && !c.isBlank()) f.add(c);
        if (!f.isEmpty()) pendingAnimEvents.offer(f);
    }

    /** View calls this — read and clear in one shot */
    public List<String> pollAnimSequence() { return pendingAnimEvents.poll(); }
    public void startDeathTimer() { startDeathTimer(DEATH_ANIM_DURATION); }

    public void startDeathTimer(float duration) { if (deathTimer < 0) deathTimer = duration; }

    public boolean isDeathAnimDone() {
        return deathTimer == 0f;
    }

    public void tickDeathTimer(float delta) {
        if (deathTimer > 0) {
            deathTimer -= delta;
            if (deathTimer < 0) deathTimer = 0f;
        }
    }
    @Override
    public void takeDamage(int damage) {
        takeDamage(damage, false);
    }

    public void takeDamage(int damage, boolean isPoisonous) {
        if (!isAlive || this.vulnerabilityState == Vulnerability.INVULNERABLE) return;

        if (isPoisonous) {
            this.hp -= damage;

            if (this.hp <= 0) {
                this.hp = 0;
                this.isAlive = false;
                this.state = ZombieActivity.DEAD;
                startDeathTimer();
                GameSession session = App.getGameSession();

                if (isGlowing) {
                    com.ussr.pvz.model.entities.items.PlantFoodDrop plantFoodDrop =
                            new com.ussr.pvz.model.entities.items.PlantFoodDrop(1);
                    plantFoodDrop.setPosition(this.getPosition());
                    if (session != null) {
                        session.addItem(plantFoodDrop);
                    }
                }

                handleDeathEffect(session);

                if (session != null) {
                    session.notifyZombieDied(this);
                }
            }
        } else {
            takeDamage(damage, null);
        }
    }

    public void setGlowing(boolean isGlowing) {
        this.isGlowing = isGlowing;
    }

    public String getPamPath() {
        return pamPath;
    }

    public void setPamPath(String pamPath) {
        this.pamPath = pamPath;
    }

    public boolean isBossMirror() {
        return bossMirror;
    }

    public void setBossMirror(boolean bossMirror) {
        this.bossMirror = bossMirror;
    }

    public ZombossController getZombossController() {
        return zombossController;
    }

    public void setZombossController(ZombossController zombossController) {
        this.zombossController = zombossController;
    }

    public enum Status{NORMAL , FREEZE , FIRED , POISONED , BUTTER , HYPNOTIZED , FROZEN_SOLID}
    private Status status = Status.NORMAL;

    private Vulnerability vulnerabilityState = Vulnerability.FULLY_VULNERABLE;

    private Faction faction = Faction.ZOMBIES;

    public Zombie(String name, Armor armor, boolean canSpawnPlantFood) {
        this.name = name;
        this.armor = armor;
        this.isGlowing = canSpawnPlantFood && RAND.nextInt(100) < 5;
    }

    public Zombie(String name, Armor armor) {
        this(name, armor, true);
    }

    @Override
    public void update(float delta) {
        GameSession session = App.getGameSession();

        if (session == null) {
            return;
        }

        if (!isAlive) {
            tickDeathTimer(delta);  // ← add this
            applyEffect(session, delta);
            return;
        }

        ZombieFactory.respawnPushedStructureIfNeeded(this);

        applyEffect(session, delta);
        updatePoison(delta);
        updateStatus(delta);
        updateActivity(session, delta);
    }

    private void applyEffect(
            GameSession session,
            float delta
    ) {
        if (effectStatus != null) {
            effectStatus.effect(this, session, delta);
        }
    }

    private void updateStatus(float delta) {
        if (statusTimeRemaining <= 0) {
            return;
        }

        statusTimeRemaining -= delta;

        if (statusTimeRemaining <= 0) {
            statusTimeRemaining = 0;

            if (status == Status.FREEZE
                    || status == Status.BUTTER
                    || status == Status.POISONED
                    || status == Status.FIRED
                    || status == Status.FROZEN_SOLID) {
                status = Status.NORMAL;
                poisonTickDamage = 0;
            }
        }
    }

    private void updatePoison(float delta) {
        if (status != Status.POISONED || statusTimeRemaining <= 0 || !isAlive) return;
        poisonTickTimer -= delta;
        if (poisonTickTimer <= 0) {
            takeDamage(poisonTickDamage, true);
            poisonTickTimer += 1.0;
        }
    }

    private void updateActivity(
            GameSession session,
            float delta
    ) {
        if (status == Status.FROZEN_SOLID) {
            return;
        }

        float movementDelta = delta * getMovementSpeedMultiplier();

        if (updateSlipperySlide(movementDelta)) {
            return;
        }

        Damageable target = acquireTarget(session);

        if (canJumpOver(target)) {
            moveBehavior.move(this, session, movementDelta);
            target = acquireTarget(session);
        }

        if (target != null && target.isAlive()) {
            state = ZombieActivity.EATING;

            if (attackBehavior != null) {
                attackBehavior.attack(
                        this,
                        session,
                        delta
                );
            }

            return;
        }

        state = ZombieActivity.WALKING;

        if (moveBehavior != null) {
            moveBehavior.move(this, session, movementDelta);
        }
    }

    public float getMovementSpeedMultiplier() {
        return switch (status) {
            case FREEZE -> FREEZE_SPEED_MULTIPLIER;
            case BUTTER -> BUTTER_SPEED_MULTIPLIER;
            case FROZEN_SOLID -> 0f;
            default -> 1.0f;
        };
    }

    private boolean canJumpOver(Damageable target) {
        return moveBehavior instanceof JumpMove jumpMove
                && target instanceof Plant plant
                && jumpMove.canFlyOver(plant);
    }

    private void handleDeathEffect(GameSession session) {
        if (effectStatus != null && session != null) {
            effectStatus.onDeath(this, session);
        }
    }

    public Damageable acquireTarget(GameSession session) {
        if (this.getPosition().x() <= -0.5) {
            Brain brain = null;

            if (session.getLevel() != null) {
                int lane = (int) this.getPosition().y();
                Object behavior = session.getLevel().getBehavior();

                if (behavior instanceof IZombieBehavior iZombie) {
                    brain = iZombie.getBrainInLane(lane);
                } else if (behavior instanceof MultiplayerIZombieBehavior multiplayer) {
                    brain = multiplayer.getBrainInLane(lane);
                } else if (behavior instanceof CouchIZombieBehavior couch) {
                    brain = couch.getBrainInLane(lane);
                }
            }

            if (brain != null && brain.isAlive()) {
                return brain;
            }
        }

        Damageable target = faction.findTarget(this, session);

        if (target instanceof Plant targetPlant) {
            double deltaX = Math.abs(this.getPosition().x() - targetPlant.getLocation().x());
            if (deltaX > 0.5) {
                return null;
            }
        } else if (target instanceof GameEntity targetEntity) {
            double deltaX = Math.abs(this.getPosition().x() - targetEntity.getPosition().x());
            if (deltaX > 0.5) {
                return null;
            }
        }

        return target;
    }

    public void hypnotize() {
        if (faction == Faction.PLANTS || !isAlive) return;
        this.faction = Faction.PLANTS;
        if (getSpeed() != null) {
            setSpeed(getSpeed().scale(-1));
        }
        this.moveBehavior = new HypnotizedMoveBehavior(this.moveBehavior);
    }

    public Faction getFaction() {
        return faction;
    }

    public boolean isHypnotized() {
        return faction == Faction.PLANTS;
    }

    public Cell getCurrentCell(GameSession session) {
        if (getPosition() == null || session.getLawn() == null) return null;
        int col = (int) getPosition().x();
        int row = (int) getPosition().y();
        return session.getLawn().getCell(row, col);
    }

    public void takeDamage(int damage, Object damageSource) {
        if (!isAlive || this.vulnerabilityState == Vulnerability.INVULNERABLE) return;

        if (this.vulnerabilityState == Vulnerability.SUBMERGED) {
            boolean allowDamage = isAllowDamage(damageSource);

            if (!allowDamage) return;
        }

        int actualDamage = damage;
        if (this.defenseBehavior != null) {
            actualDamage = this.defenseBehavior.handleDamage(this, damage, damageSource, App.getGameSession());
        }

        if (actualDamage > 0) {
            applyDamageCalculations(actualDamage, damageSource);
        }
    }

    private boolean isAllowDamage(Object damageSource) {
        boolean allowDamage = false;

        if (damageSource instanceof Plant plant) {
            String plantName = plant.getName().toLowerCase().replace("-", "").replace(" ", "");

            if (damageWhileSubmerged != null && damageWhileSubmerged.contains(plantName)) {
                allowDamage = true;
            } else if (plant.getPlantFoodTimer() > 0 && damageWhileSubmergedPlantfoodOnly != null &&
                    damageWhileSubmergedPlantfoodOnly.contains(plantName)) {
                allowDamage = true;
            }
        } else if (damageSource instanceof Projectile p) {
            if (p.getMoveStrategy() instanceof ArcMove) {
                allowDamage = true;
            }
        }
        return allowDamage;
    }

    private void applyDamageCalculations(int damage, Object damageSource) {
        int remaining = damage;

        if (pushedStructure != null && pushedStructure.isAlive()) {
            remaining = pushedStructure.absorbDamage(remaining);
        }

        if (remaining > 0 && armor != null && !armor.isDestroyed()) {
            remaining = armor.takeDamage(remaining);
        }

        if (remaining > 0) {
            hp -= remaining;
            if (hp <= 0) {
                hp = 0;
                isAlive = false;
                state = ZombieActivity.DEAD;
                startDeathTimer();
                GameSession session = App.getGameSession();

                if (isGlowing) {
                    PlantFoodDrop plantFoodDrop = new PlantFoodDrop(1);
                    plantFoodDrop.setPosition(this.getPosition());
                    if (session != null) {
                        session.addItem(plantFoodDrop);
                    }
                }
                handleDeathEffect(session);

                if (session != null) {
                    String killerName = resolveKillerName(damageSource);
                    session.notifyZombieDied(this, killerName);
                }
            }
        }
    }

    // NOTE: Projectiles don't currently carry a reference back to the plant that fired them,
    // so exact killer attribution isn't available for projectile kills yet. This resolves what
    // it can and falls back to "Unknown" rather than guessing. Wiring a real owner reference
    // through Plant -> ShootStrategy -> Projectile is a larger, separate change.
    private String resolveKillerName(Object damageSource) {
        if (damageSource instanceof com.ussr.pvz.model.entities.plants.Plant plant) {
            return plant.getName();
        }
        return "Unknown";
    }

    public Vulnerability getVulnerabilityState() {
        return vulnerabilityState;
    }

    public void setVulnerabilityState(Vulnerability state) {
        this.vulnerabilityState = state;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
        if (this.maxHp <= 0) {
            this.maxHp = hp;
        }
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public double getEatDps() {
        return eatDps;
    }

    public void setEatDps(double eatDps) {
        this.eatDps = eatDps;
    }

    public ZombieSize getSize() {
        return size;
    }

    public void setSize(ZombieSize size) {
        this.size = size;
    }

    public ZombieActivity getState() {
        return state;
    }

    public Armor getArmor() {
        return armor;
    }

    public MoveBehavior getMoveBehavior() {
        return moveBehavior;
    }

    public void setMoveBehavior(MoveBehavior moveBehavior) {
        this.moveBehavior = moveBehavior;
    }

    public AttackBehavior getAttackBehavior() {
        return attackBehavior;
    }

    public void setAttackBehavior(AttackBehavior attackBehavior) {
        this.attackBehavior = attackBehavior;
    }

    public DefenseBehavior getDefenseBehavior() {
        return defenseBehavior;
    }

    public void setDefenseBehavior(DefenseBehavior defenseBehavior) {
        this.defenseBehavior = defenseBehavior;
    }

    public EffectStatus getEffectStatus() {
        return effectStatus;
    }

    public void setEffectStatus(EffectStatus effectStatus) {
        this.effectStatus = effectStatus;
    }

    public boolean isGlowing() {
        return isGlowing;
    }

    public String getAlias() {
        return name;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        setStatus(status, getDefaultStatusDuration(status));
    }

    public void setStatus(Status status, double duration) {
        this.status = status;
        this.statusTimeRemaining = Math.max(0.0, duration);
    }

    public static double getDefaultStatusDuration(Status status) {
        return switch (status) {
            case FREEZE -> DEFAULT_FREEZE_DURATION;
            case BUTTER -> DEFAULT_BUTTER_DURATION;
            case POISONED -> DEFAULT_POISON_DURATION;
            case FIRED -> DEFAULT_FIRE_DURATION;
            case FROZEN_SOLID -> DEFAULT_FREEZE_DURATION;
            default -> 0.0;
        };
    }

    public void applyPoison(int tickDamage, double duration) {
        this.status = Status.POISONED;
        this.statusTimeRemaining = Math.max(this.statusTimeRemaining, duration);
        this.poisonTickDamage = Math.max(this.poisonTickDamage, tickDamage);
        this.poisonTickTimer = 1.0;
    }

    public double getStatusTimeRemaining() {
        return statusTimeRemaining;
    }

    public boolean isAnimationPaused() {
        return status == Status.FROZEN_SOLID;
    }

    public void setArmor(Armor armor) {
        this.armor = armor;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }

    public PushableStructure getPushedStructure() {
        return pushedStructure;
    }

    public void setPushedStructure(PushableStructure pushedStructure) {
        this.pushedStructure = pushedStructure;
    }

    public int getPushableRespawnsRemaining() {
        return pushableRespawnsRemaining;
    }

    public void setPushableRespawnsRemaining(int pushableRespawnsRemaining) {
        this.pushableRespawnsRemaining = pushableRespawnsRemaining;
    }

    public void setDamageWhileSubmerged(java.util.List<String> damageWhileSubmerged) {
        this.damageWhileSubmerged = damageWhileSubmerged;
    }

    public void setDamageWhileSubmergedPlantfoodOnly(java.util.List<String> damageWhileSubmergedPlantfoodOnly) {
        this.damageWhileSubmergedPlantfoodOnly = damageWhileSubmergedPlantfoodOnly;
    }

    public void setState(ZombieActivity state) { this.state = state; }

    public boolean isSlidingBetweenRows() {
        return slidingBetweenRows;
    }

    public void startSlipperySlide(double targetRow) {
        if (slidingBetweenRows || getPosition() == null) {
            return;
        }

        slidingBetweenRows = true;
        slipperySlideElapsed = 0f;
        slipperyStartRow = getPosition().y();
        slipperyTargetRow = targetRow;
    }

    private boolean updateSlipperySlide(float delta) {
        if (!slidingBetweenRows || getPosition() == null) {
            return false;
        }

        slipperySlideElapsed = Math.min(
                SLIPPERY_SLIDE_DURATION,
                slipperySlideElapsed + delta
        );

        double progress =
                slipperySlideElapsed / SLIPPERY_SLIDE_DURATION;

        // Smoothstep prevents the movement from starting or stopping sharply.
        double smoothProgress =
                progress * progress * (3.0 - 2.0 * progress);

        double currentRow =
                slipperyStartRow
                        + (slipperyTargetRow - slipperyStartRow)
                        * smoothProgress;

        setPosition(Vec2.of(
                getPosition().x(),
                currentRow
        ));

        if (progress >= 1.0) {
            setPosition(Vec2.of(
                    getPosition().x(),
                    slipperyTargetRow
            ));

            slidingBetweenRows = false;
        }

        return true;
    }
}