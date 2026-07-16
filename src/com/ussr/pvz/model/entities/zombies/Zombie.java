package com.ussr.pvz.model.entities.zombies;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.board.structures.PushableStructure;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.PlantFoodDrop;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.attack.AttackBehavior;
import com.ussr.pvz.model.entities.zombies.defense.DefenseBehavior;
import com.ussr.pvz.model.entities.zombies.effect.EffectStatus;
import com.ussr.pvz.model.entities.zombies.move.HypnotizedMoveBehavior;
import com.ussr.pvz.model.entities.zombies.move.MoveBehavior;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy; // Assumes your ArcMove implements an interface/class like this
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;
import com.ussr.pvz.service.game.ZombieService;

import java.util.Random;

public class Zombie extends GameEntity implements Damageable {

    private final ZombieService zombieService = new ZombieService();

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
    private ZombieSize size;
    private ZombieActivity state = ZombieActivity.WALKING;
    private final boolean isGlowing;
    private java.util.List<String> damageWhileSubmerged;
    private java.util.List<String> damageWhileSubmergedPlantfoodOnly;

    @Override
    public void takeDamage(int damage) {
        takeDamage(damage, false);
    }

    public void takeDamage(int damage, boolean isPoisonous) {
        if (!isAlive || this.vulnerabilityState == Vulnerability.INVULNERABLE) return;

        if (isPoisonous) {
            // Poison bypasses armor and defense calculations directly
            this.hp -= damage;

            if (this.hp <= 0) {
                this.hp = 0;
                this.isAlive = false;
                this.state = ZombieActivity.DEAD;

                GameSession session = App.getGameSession();

                // Handle plant food drops on death
                if (isGlowing) {
                    com.ussr.pvz.model.entities.items.PlantFoodDrop plantFoodDrop =
                            new com.ussr.pvz.model.entities.items.PlantFoodDrop(1);
                    plantFoodDrop.setPosition(this.getPosition());
                    if (session != null) {
                        session.addItem(plantFoodDrop);
                    }
                }

                if (session != null) {
                    session.notifyZombieDied(this);
                }
            }
        } else {
            // Route standard damage back to the main pipeline
            takeDamage(damage, null);
        }
    }

    public enum Status{NORMAL , FREEZE , FIRED , POISONED , BUTTER , HYPNOTIZED}
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
    public void tick() {
        GameSession session = App.getGameSession();
        if (session == null) return;

        if (!isAlive) {
            if (effectStatus != null) effectStatus.effect(this, session);
            return;
        }

        ZombieFactory.respawnPushedStructureIfNeeded(this);

        if (effectStatus != null) effectStatus.effect(this, session);

        if (statusTimeRemaining > 0) {
            statusTimeRemaining -= com.ussr.pvz.model.engine.GameClock.SECONDS_PER_TICK;
            if (statusTimeRemaining <= 0) {
                statusTimeRemaining = 0;
                if (status == Status.FREEZE || status == Status.BUTTER) {
                    status = Status.NORMAL;
                }
            }
        }

        // One single source of truth for targeting!
        Damageable target = acquireTarget(session);

        if (target != null && target.isAlive()) {
            state = ZombieActivity.EATING;
            if (attackBehavior != null) {
                attackBehavior.attack(this, session);
            }
        } else {
            state = ZombieActivity.WALKING;
            if (moveBehavior != null) {
                moveBehavior.move(this, session);
            }
        }
    }

    public Damageable acquireTarget(GameSession session) {
        return faction.findTarget(this, session);
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
            boolean allowDamage = false;

            if (damageSource instanceof com.ussr.pvz.model.entities.plants.Plant plant) {
                String plantName = plant.getName().toLowerCase().replace("-", "").replace(" ", "");

                // Check standard submerged damage whitelist (e.g. Tangle Kelp, Ghost Pepper)
                if (damageWhileSubmerged != null && damageWhileSubmerged.contains(plantName)) {
                    allowDamage = true;
                }
                // Check if the plant is currently using Plant Food and matches the secondary whitelist
                else if (plant.getPlantFoodTimer() > 0 && damageWhileSubmergedPlantfoodOnly != null && damageWhileSubmergedPlantfoodOnly.contains(plantName)) {
                    allowDamage = true;
                }
            } else if (damageSource instanceof Projectile p) {
                // As a fallback for projectiles without an explicit Plant origin, Lobbed (ArcMove) shots always hit
                if (p.getMoveStrategy() instanceof ArcMove) {
                    allowDamage = true;
                }
            }

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

    private void applyDamageCalculations(int damage, Object damageSource) {
        int remaining = damage;

        if (armor != null && !armor.isDestroyed()) {
            remaining = armor.takeDamage(damage);
        }

        if (remaining > 0) {
            hp -= remaining;
            if (hp <= 0) {
                hp = 0;
                isAlive = false;
                state = ZombieActivity.DEAD;

                GameSession session = App.getGameSession();

                if (isGlowing) {
                    PlantFoodDrop plantFoodDrop = new PlantFoodDrop(1);
                    plantFoodDrop.setPosition(this.getPosition());
                    if (session != null) {
                        session.addItem(plantFoodDrop);
                    }
                }

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
        this.status = status;
        this.statusTimeRemaining = switch (status) {
            case FREEZE -> 5.0;
            case BUTTER -> 4.0;
            default -> 0.0;
        };
    }

    public double getStatusTimeRemaining() {
        return statusTimeRemaining;
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
}