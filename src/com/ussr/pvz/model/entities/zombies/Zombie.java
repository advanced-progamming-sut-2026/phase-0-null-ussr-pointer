package com.ussr.pvz.model.entities.zombies;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.board.Cell;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.GameSession;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.attack.AttackBehavior;
import com.ussr.pvz.model.entities.zombies.defense.DefenseBehavior;
import com.ussr.pvz.model.entities.zombies.effect.EffectStatus;
import com.ussr.pvz.model.entities.zombies.move.MoveBehavior;

public class Zombie extends GameEntity {
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

    public Zombie(String name, Armor armor) {
        this.name = name;
        this.armor = armor;
    }

    @Override
    public void tick() {
        if (!isAlive) return;

        GameSession session = App.getGameSession();
        if (session == null) return;

        Plant target = getTargetPlant(session);

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

    public Plant getTargetPlant(GameSession session) {
        int col = (int) getPosition().x();
        int row = (int) getPosition().y();
        Cell cell = session.getLawn().getCell(row, col);
        if (cell == null) return null;
        Plant plant = cell.getPlant();
        if (plant != null && plant.isAlive()) return plant;
        return null;
    }

    public void takeDamage(int damage) {
        if (!isAlive) return;

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
            }
        }
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

    @Override
    public String toString() {
        return String.format("%s | hp: %d | state: %s | pos: (%.1f, %.1f)%s",
                name, hp, state,
                getPosition() != null ? getPosition().x() : 0,
                getPosition() != null ? getPosition().y() : 0,
                armor != null && !armor.isDestroyed()
                        ? " | armor: " + armor.getArmorHp() + "/" + armor.getMaxArmorHp()
                        : "");
    }
}