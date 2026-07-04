package com.ussr.pvz.model.entities.zombies;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
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

import java.util.Random;

public class Zombie extends GameEntity implements Damageable {
    //todo if iced bullet hit prospector the move strategy should change from prospector to normal
    private static final Random RAND = new Random();
    private final String name;

    private MoveBehavior moveBehavior;
    private EffectStatus effectStatus;
    private DefenseBehavior defenseBehavior;
    //todo the zombie may have several attack behavior(eat and throw the bullets) or we should take care of them in effects as we did for laser
    private AttackBehavior attackBehavior;
    private Armor armor;

    private int hp;
    private double eatDps;
    private ZombieSize size;
    private ZombieActivity state = ZombieActivity.WALKING;
    private final boolean isGlowing;

    @Override
    public void takeDamage(int damage) {

    }

    public enum Status{NORMAL , FREEZE , FIRED , POISONED}
    private Status status = Status.NORMAL;

    private Vulnerability vulnerabilityState = Vulnerability.FULLY_VULNERABLE;

    private Faction faction = Faction.ZOMBIES;

    public Zombie(String name, Armor armor) {
        this.name = name;
        this.armor = armor;
        this.isGlowing = RAND.nextInt(100) < 5;
    }

    @Override
    public void tick() {
        if (!isAlive) return;
        GameSession session = App.getGameSession();
        if (session == null) return;

        Damageable target = acquireTarget(session);
        if (target != null && target.isAlive()) {
            state = ZombieActivity.EATING;
            if (attackBehavior != null) attackBehavior.attack(this, session);
        } else {
            state = ZombieActivity.WALKING;
            if (moveBehavior != null) moveBehavior.move(this, session);
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
            if (damageSource instanceof Projectile p && !(p.getMoveStrategy() instanceof ArcMove)) {
                return;
            }
        }

        int actualDamage = damage;
        if (this.defenseBehavior != null) {
            actualDamage = this.defenseBehavior.handleDamage(this, damage, damageSource, App.getGameSession());
        }

        if (actualDamage > 0) {
            applyDamageCalculations(actualDamage);
        }
    }

    private void applyDamageCalculations(int damage) {
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

                if (isGlowing) {
                    PlantFoodDrop plantFoodDrop = new PlantFoodDrop(1);
                    plantFoodDrop.setPosition(this.getPosition());
                    GameSession session = App.getGameSession();
                    if (session != null) {
                        session.getItems().add(plantFoodDrop);
                    }
                }
            }
        }
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
    }

    public void setArmor(Armor armor) {
        this.armor = armor;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }
}