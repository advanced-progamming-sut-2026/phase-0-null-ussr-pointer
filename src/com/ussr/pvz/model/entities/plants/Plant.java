package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.engine.GameClock;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.modifiers.ModifiableStat;
import com.ussr.pvz.model.entities.plants.actstrategy.ActStrategy;
import com.ussr.pvz.model.entities.plants.actstrategy.ModifyStrategy;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodEffect;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodType;
import com.ussr.pvz.model.entities.zombies.Faction;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Plant extends GameEntity implements Damageable {
    private int id;
    private String name;
    private int level = 1;
    private int hp;
    private int recharge;
    private double actionInterval;
    private int cost;
    private Location location;
    private final ArrayList<Tag> tags = new ArrayList<>();
    private int damage;
    private PlantType type;
    private Plant bottom = null;

    private ModifiableStat hpStat;
    private ModifiableStat actionIntervalStat;

    private ActStrategy actStrategy;
    private PlantFoodEffect plantFoodEffect;
    private PlantFoodType plantFoodType;
    private double internalTimer = 0.0;
    private double abilityValue;

    private GrowthTracker growthTracker;

    //for now incapacitated is for all cat/sheep/sctopus but we can change it in the future
    public enum PlantState {
        ACTIVE,
        INCAPACITATED,
        PREPPING,
        DYING
    }

    private PlantState state;

    // Kept to track applied upgrades
    private final List<String> rawUpgrades = new ArrayList<>();
    //avoid hardcode
    private List<Vec2> shootingVectors = new ArrayList<>();

    public Plant() {
    }

    // Deep copy constructor used by the factory to clone blueprints
    public Plant(Plant blueprint) {
        this.id = blueprint.id;
        this.name = blueprint.name;
        this.type = blueprint.type;
        this.cost = blueprint.cost;
        this.hp = blueprint.hp;
        this.damage = blueprint.damage;
        this.actionInterval = blueprint.actionInterval;
        this.recharge = blueprint.recharge;
        this.tags.addAll(blueprint.tags);
        this.rawUpgrades.addAll(blueprint.rawUpgrades);
        this.state = PlantState.ACTIVE;
        //todo needs check
        this.location = blueprint.location;
        // Strategy attachments
        this.actStrategy = blueprint.actStrategy;
        this.plantFoodEffect = blueprint.plantFoodEffect;
        this.growthTracker = blueprint.growthTracker;

        // Initialize wrapper stats
        this.hpStat = new ModifiableStat(this.hp);
        this.actionIntervalStat = new ModifiableStat((float) this.actionInterval);
        this.abilityValue = blueprint.abilityValue;
    }

    @Override
    public void tick() {
        if (!isAlive || state == PlantState.INCAPACITATED) return;

        if (hpStat != null) hpStat.update((float) GameClock.SECONDS_PER_TICK);
        if (actionIntervalStat != null) actionIntervalStat.update((float) GameClock.SECONDS_PER_TICK);
        if (growthTracker != null) growthTracker.update(GameClock.SECONDS_PER_TICK);

        if (actStrategy == null) return;

        internalTimer += GameClock.SECONDS_PER_TICK;

        double interval = actionIntervalStat != null
                ? actionIntervalStat.getValue()
                : actionInterval;

        if (internalTimer <= 0) {
            internalTimer = (actionIntervalStat != null ? actionIntervalStat.getValue() : actionInterval);
            actStrategy.act(this, com.ussr.pvz.model.App.getGameSession());
        }
    }

    @Override
    public void takeDamage(int damage) {
        takeDamage(damage, null);
    }

    public void takeDamage(int damage, Zombie dealer) {
        if (!isAlive) return;
        int newHp = getHp() - damage;
        if (newHp <= 0) {
            setHp(0);
            isAlive = false;
            if (dealer != null && this.actStrategy instanceof ModifyStrategy && this.getTags().contains(Tag.MAGIC))
                dealer.setFaction(Faction.PLANTS);
        } else {
            setHp(newHp);
        }
    }

    public void updateGrowth(double deltaTimeSeconds) {
        if (growthTracker != null) growthTracker.update(deltaTimeSeconds);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getHp() {
        return hpStat != null ? (int) hpStat.getValue() : hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
        if (hpStat != null) hpStat.setBaseValue(hp);
    }

    public int getRecharge() {
        return recharge;
    }

    public void setRecharge(int recharge) {
        this.recharge = recharge;
    }

    public double getActionInterval() {
        return actionIntervalStat != null ? actionIntervalStat.getValue() : actionInterval;
    }

    public void setActionInterval(double actionInterval) {
        this.actionInterval = actionInterval;
        if (actionIntervalStat != null) actionIntervalStat.setBaseValue((int) actionInterval);
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getDamage() {
        if (growthTracker != null) {
            Double staged = growthTracker.getStageValue("damage");
            if (staged != null) return staged.intValue();
        }
        return this.damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public PlantType getType() {
        return type;
    }

    public void setType(PlantType type) {
        this.type = type;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public List<String> getRawUpgrades() {
        return rawUpgrades;
    }

    public void setActStrategy(ActStrategy actStrategy) {
        this.actStrategy = actStrategy;
    }

    public ActStrategy getActStrategy() {
        return actStrategy;
    }

    public void setPlantFoodEffect(PlantFoodEffect plantFoodEffect) {
        this.plantFoodEffect = plantFoodEffect;
    }

    public PlantFoodEffect getPlantFoodEffect() {
        return plantFoodEffect;
    }

    public Plant getBottom() {
        return bottom;
    }

    public void setBottom(Plant bottom) {
        this.bottom = bottom;
    }

    public PlantFoodType getPlantFoodType() {
        return plantFoodType;
    }

    public void setPlantFoodType(PlantFoodType plantFoodType) {
        this.plantFoodType = plantFoodType;
    }
    public double getAbilityValue() {
        if (growthTracker != null) {
            Double staged = growthTracker.getStageValue("abilityValue");
            if (staged != null) return staged;
        }
        return this.abilityValue;
    }

    public List<Map<String, Object>> getWrampUp() {
        return growthTracker != null ? growthTracker.getRawStages() : null;
    }

    public void setWrampUp(List<Map<String, Object>> wrampUp) {
        this.growthTracker = (wrampUp != null && !wrampUp.isEmpty()) ? new GrowthTracker(wrampUp) : null;
    }

    public int getCurrentStage() {
        return growthTracker != null ? growthTracker.getCurrentStage() : 1;
    }

    public record Location(int x, int y) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Plant.Location(int x1, int y1))) return false;
            return x == x1 && y == y1;
        }
    }

    public double getIntervalTimer() { return internalTimer; }

    public void setInternalTimer(double timer) { this.internalTimer = timer; }

    public void setAbilityValue(double value) { this.abilityValue = value; }

    public List<Vec2> getShootingVectors() {
        return shootingVectors;
    }

    public void setShootingVectors(List<Vec2> shootingVectors) {
        this.shootingVectors = shootingVectors;
    }

    public void addShootingVectors(Vec2 vec2) {
        shootingVectors.add(vec2);
    }

    public void setState(PlantState state) {
        this.state = state;
    }

    public PlantState getState() {
        return this.state;
    }
}