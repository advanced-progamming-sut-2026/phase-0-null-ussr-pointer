package com.ussr.pvz.model.entities.zombies;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.items.PlantFoodDrop;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.attack.AttackBehavior;
import com.ussr.pvz.model.entities.zombies.defense.DefenseBehavior;
import com.ussr.pvz.model.entities.zombies.effect.EffectStatus;
import com.ussr.pvz.model.entities.zombies.move.MoveBehavior;
import com.ussr.pvz.model.entities.projectiles.move.MoveStrategy; // Assumes your ArcMove implements an interface/class like this
import com.ussr.pvz.model.entities.projectiles.move.ArcMove;

import java.util.Random;

public class Zombie extends GameEntity {
    //todo if iced bullet hit prospector the move strategy should change from prospector to normal
    private static final Random RAND = new Random();
    private final String name;

    private MoveBehavior moveBehavior;
    private EffectStatus effectStatus;
    private DefenseBehavior defenseBehavior;
    private AttackBehavior attackBehavior;
    private final Armor armor;

    private int hp;
    private double eatDps;
    private ZombieSize size;
    private ZombieActivity state = ZombieActivity.WALKING;
    private final boolean isGlowing;
    public enum Status{NORMAL , FREEZE , FIRED , POISONED}
    private Status status = Status.NORMAL;

    // --- Core tactical vulnerability property ---
    private Vulnerability vulnerabilityState = Vulnerability.FULLY_VULNERABLE;

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

        Plant target = getTargetPlant(session);
        if (target != null && target.isAlive()) {
            state = ZombieActivity.EATING;
            if (attackBehavior != null) attackBehavior.attack(this, session);
        } else {
            state = ZombieActivity.WALKING;
            if (moveBehavior != null) moveBehavior.move(this, session);
        }
    }

    // --- Overloaded: Fallback for generic damage sources (e.g., spikes, explosions) ---
    public void takeDamage(int damage) {
        // Generic damage hits submerged targets normally, but is blocked by total invulnerability
        if (this.vulnerabilityState == Vulnerability.INVULNERABLE) return;
        applyDamageCalculations(damage);
    }

    public void takeDamage(int damage, Object moveStrategy) {
        if (!isAlive) return;

        if (this.vulnerabilityState == Vulnerability.INVULNERABLE) return;

        if (this.vulnerabilityState == Vulnerability.SUBMERGED) {
            if (!(moveStrategy instanceof ArcMove)) {
                return;
            }
        }

        applyDamageCalculations(damage);
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

    public Plant getTargetPlant(GameSession session) {
        if (getPosition() == null) return null;
        int col = (int) getPosition().x();
        int row = (int) getPosition().y();
        Cell cell = session.getLawn().getCell(row, col);
        if (cell == null) return null;
        Plant plant = cell.getPlant();
        if (plant != null && plant.isAlive() && !plant.isCat()) return plant;
        return null;
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
}