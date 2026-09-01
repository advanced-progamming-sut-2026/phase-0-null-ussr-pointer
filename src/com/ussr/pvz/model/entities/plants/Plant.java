package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.entities.plants.animation.PlantAnimationController;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.modifiers.ModifiableStat;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.actstrategy.ActStrategy;
import com.ussr.pvz.model.entities.plants.actstrategy.ImitaterStrategy;
import com.ussr.pvz.model.entities.plants.actstrategy.WallNutStrategy;
import com.ussr.pvz.model.entities.plants.actstrategy.MeleeStrategy;
import com.ussr.pvz.model.entities.plants.actstrategy.ShockwaveStrategy;
import com.ussr.pvz.model.entities.plants.actstrategy.SunProduceStrategy;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodEffect;
import com.ussr.pvz.model.entities.plants.plantfood.PlantFoodType;
import com.ussr.pvz.model.entities.plants.upgrades.SpecialUpgrade;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.util.Vec2;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Plant extends GameEntity implements Damageable {
    public static final int MAX_PEA_POD_STACK = 5;
    public static final int MAX_CHILL_LEVEL = 3;
    private static final double CHILL_LEVEL_ONE_INTERVAL_MULTIPLIER = 1.5;
    private static final double CHILL_LEVEL_TWO_INTERVAL_MULTIPLIER = 2.0;
    private int id;
    private String name;
    private int level = 1;
    private int hp;
    int maxHp;
    private double maxRecharge;
    private double recharge;
    private double actionInterval;
    double attackOffset = -1.0;
    private int cost;
    private final ArrayList<Tag> tags = new ArrayList<>();
    private int damage;
    private PlantType type;
    private Plant bottom = null;
    private int stackNumber = 1;
    private boolean isBuffed = false;

    ModifiableStat hpStat;
    ModifiableStat actionIntervalStat;

    private ActStrategy actStrategy;
    private PlantFoodEffect plantFoodEffect;
    private PlantFoodType plantFoodType;
    private double internalTimer = 0.0;
    private double abilityValue;
    private int chillLevel = 0;
    GrowthTracker growthTracker;
    private List<Vec2> projectileOrigins = new ArrayList<>();
    private String pamPath;
    private String projectilePam;
    private String hitPam;
    private String plantFoodProjectilePam;
    private String plantFoodHitPam;
    private String butterHitPam;

    private final PlantAnimationController animationController = new PlantAnimationController();
    private double plantFoodTimer = 0.0;
    double plantFoodDuration = 4.0;
    private int plantFoodAnimReplays = 1;
    private int plantFoodIntroReplaysRemaining = 0;
    private boolean plantFoodIntroActive = false;

    private PlantArmor armor;

    double lifetime = Double.MAX_VALUE;
    int remainingSmashes = -1;

    public String getPamPath() {
        return pamPath;
    }

    public void setPamPath(String pamPath) {
        this.pamPath = pamPath;
    }

    public String getProjectilePam() {
        return projectilePam;
    }

    public void setProjectilePam(String projectilePam) {
        this.projectilePam = projectilePam;
    }

    public String getHitPam() {
        return hitPam;
    }

    public void setHitPam(String hitPam) {
        this.hitPam = hitPam;
    }

    public String getPlantFoodProjectilePam() {
        return plantFoodProjectilePam;
    }

    public void setPlantFoodProjectilePam(String plantFoodProjectilePam) {
        this.plantFoodProjectilePam = plantFoodProjectilePam;
    }

    public String getPlantFoodHitPam() {
        return plantFoodHitPam;
    }

    public void setPlantFoodHitPam(String plantFoodHitPam) {
        this.plantFoodHitPam = plantFoodHitPam;
    }

    public String getButterHitPam() {
        return butterHitPam;
    }

    public void setButterHitPam(String butterHitPam) {
        this.butterHitPam = butterHitPam;
    }

    public String getAnimationClip() {
        return AnimClips.getAnimationClip(this);
    }

    public enum PlantState {
        ACTIVE,
        INCAPACITATED,
        PREPPING,
        DYING,
        IMITATE_IDLE,
        IMITATE_ATTACK
    }

    private boolean imitationOverlayActive = false;
    private String imitationTargetName;
    private boolean justTransformed = false;

    private PlantState state;
    private float deathAnimationTimer;
    private float deathImpactTimer;
    private Runnable deathImpactAction;
    private float pendingAttackTimer;
    private Runnable pendingAttackAction;
    private boolean mineArmed;
    private float mineRecoverTimer;
    private double defensiveReactionCharge;

    final EnumMap<SpecialUpgrade, Double> specialUpgrades =
            new EnumMap<>(SpecialUpgrade.class);
    private List<Vec2> shootingVectors = new ArrayList<>();

    public Plant() {
    }

    public Plant(Plant blueprint) {
        copyFrom(blueprint);
    }

    @Override
    public void update(float delta) {
        if (!isAlive && state != PlantState.DYING) return;
        animationController.update(delta);
        if (handleDyingState(delta)) return;
        if (state == PlantState.INCAPACITATED) {
            animationController.playIncapacitated();
            return;
        }
        if (state == PlantState.IMITATE_IDLE || state == PlantState.IMITATE_ATTACK) return;

        processPendingAttack(delta);

        lifetime -= delta;
        if (lifetime < 0) {
            killPlant();
            return;
        }

        updateStats(delta);
        updatePlantFood(delta);
        updateGrowth(delta);

        boolean pauseAction = isBuffed && (plantFoodEffect == null || plantFoodEffect.pausesNormalAction());
        if (pauseAction || actStrategy == null) return;

        updateAction(delta);
    }

    private boolean handleDyingState(float delta) {
        if (state != PlantState.DYING) return false;

        deathAnimationTimer -= delta;
        if (deathImpactAction != null) {
            deathImpactTimer -= delta;
            if (deathImpactTimer <= 0f) {
                Runnable impact = deathImpactAction;
                deathImpactAction = null;
                impact.run();
            }
        }
        if (deathAnimationTimer <= 0f) {
            if (deathImpactAction != null) {
                Runnable impact = deathImpactAction;
                deathImpactAction = null;
                impact.run();
            }
            deathAnimationTimer = 0f;
            isAlive = false;
            state = PlantState.ACTIVE;
        }
        return true;
    }

    private void processPendingAttack(float delta) {
        if (pendingAttackAction != null) {
            pendingAttackTimer -= delta;
            if (pendingAttackTimer <= 0f) {
                Runnable attack = pendingAttackAction;
                pendingAttackAction = null;
                attack.run();
            }
        }
    }

    private void killPlant() {
        applySpecialDeathEffect();
        isAlive = false;

        App.getGameSession()
                .getEventBus()
                .publish(new GameEvent.PlantDied(this));
    }

    private void applySpecialDeathEffect() {
        int radius = getSpecialUpgradeInt(SpecialUpgrade.DEATH_EXPLOSION_AOE);
        GameSession session = App.getGameSession();
        if (radius <= 0 || session == null || getPosition() == null) return;
        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombie.isAlive()
                    && zombie.getPosition().distanceTo(getPosition()) <= radius + 0.5) {
                zombie.takeDamage(500, this);
            }
        }
    }

    private void updateStats(float delta) {
        if (hpStat != null) hpStat.update(delta);
        if (actionIntervalStat != null) actionIntervalStat.update(delta);
    }

    private void updatePlantFood(float delta) {
        if (!isBuffed) return;

        plantFoodTimer -= delta;

        if (plantFoodTimer <= 0f) {
            isBuffed = false;
            plantFoodIntroActive = false;
            plantFoodIntroReplaysRemaining = 0;
            return;
        }

        if (plantFoodEffect != null) {
            plantFoodEffect.tickDurationEffect(this, App.getGameSession(), delta);
        }
    }

    private void updateAction(float delta) {
        internalTimer += delta;
        double interval = getActionInterval() * getChillIntervalMultiplier();

        if (isPotatoMine()) {
            updatePotatoMineAction(delta, interval);
            return;
        }

        if (internalTimer >= interval) {
            actStrategy.act(this, App.getGameSession());
            internalTimer -= interval;
        }
    }

    private void updatePotatoMineAction(float delta, double interval) {
        if (!mineArmed) {
            if (internalTimer >= interval) {
                mineArmed = true;
                mineRecoverTimer = 0.83f;
                internalTimer = 0.0;
            }
            return;
        }

        if (mineRecoverTimer > 0f) {
            mineRecoverTimer = Math.max(0f, mineRecoverTimer - delta);
            return;
        }

        actStrategy.act(this, App.getGameSession());
    }

    @Override
    public void takeDamage(int damage) {
        takeDamage(damage, null);
    }

    public void takeDamage(int damage, Zombie dealer) {
        if (!isAlive || state == PlantState.DYING || isIndestructibleIceTrap()) return;

        int remainingDamage = damage;
        if (this.armor != null && !this.armor.isDestroyed()) {
            remainingDamage = this.armor.absorbDamage(remainingDamage, this);
            this.armor.handleReflection(dealer, this);
            if (this.armor.isDestroyed()) this.armor = null;
        }

        if (remainingDamage > 0) {
            if (this.actStrategy instanceof WallNutStrategy strategy) {
                strategy.onDamageAct(this, dealer, Math.min(getHp(), remainingDamage));
            }
            int newHp = getHp() - remainingDamage;
            if (newHp <= 0) {
                handleFatalDamage(dealer);
            } else {
                setHp(newHp);
            }
        }
    }

    private void handleFatalDamage(Zombie dealer) {
        if (name.equalsIgnoreCase("Hypno-shroom")) {
            if (dealer instanceof Zombie zombie) {
                applyHypnoEffect(zombie);
            }
        } else if (this.actStrategy instanceof WallNutStrategy wallNutStrategy
                && !"Explode-o-nut".equalsIgnoreCase(name)) {
            wallNutStrategy.onDie(this);
        }

        applySpecialDeathEffect();
        setHp(0);

        if ("Explode-o-nut".equalsIgnoreCase(name)) {
            WallNutStrategy strategy = (WallNutStrategy) actStrategy;
            beginDeathAnimation(1.0f, 0.85f, () -> strategy.onDie(this));
        } else {
            isAlive = false;
        }
    }

    private void applyHypnoEffect(Zombie zombie) {
        double hpMultiplier = getSpecialUpgradeValue(SpecialUpgrade.ZOMBIE_HEALTH_MULTIPLIER);
        double damageMultiplier = getSpecialUpgradeValue(SpecialUpgrade.ZOMBIE_DAMAGE_MULTIPLIER);
        if (hpMultiplier > 0) {
            zombie.setMaxHp((int) Math.round(zombie.getMaxHp() * hpMultiplier));
            zombie.setHp((int) Math.round(zombie.getHp() * hpMultiplier));
        }
        if (damageMultiplier > 0) {
            zombie.setEatDps(zombie.getEatDps() * damageMultiplier);
        }
        zombie.hypnotize();
    }

    public void updateGrowth(double deltaTimeSeconds) {
        if (growthTracker != null && !"Kiwibeast".equalsIgnoreCase(name)) {
            double reduction = getSpecialUpgradeValue(SpecialUpgrade.GROW_TIME_REDUCTION);
            double speedMultiplier = reduction < 0 ? 1.0 + (-reduction / 24.0) : 1.0;
            growthTracker.update(deltaTimeSeconds * speedMultiplier);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLocation(Location location) {
    }

    public Location getLocation() {
        if (this.getPosition() == null) return null;
        return new Location((int) this.getPosition().x(), (int) this.getPosition().y());
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
        if (maxHp <= 0 || hp > maxHp) {
            maxHp = hp;
        }
        if (hpStat != null) hpStat.setBaseValue(hp);
    }

    public int getMaxHp() {
        return maxHp;
    }

    public double getRecharge() {
        return recharge;
    }

    public void setRecharge(double recharge) {
        this.recharge = recharge;
    }

    public double getMaxRecharge() {
        return maxRecharge;
    }

    public void setMaxRecharge(double maxRecharge) {
        this.maxRecharge = maxRecharge;
    }

    public void tickRecharge(double deltaSeconds) {
        if (recharge > 0) {
            recharge = Math.max(0.0, recharge - deltaSeconds);
        }
    }

    public void setRecharge(int recharge) {
        this.recharge = recharge;
    }

    public double getActionInterval() {
        return actionIntervalStat != null ? actionIntervalStat.getValue() : actionInterval;
    }

    public int addAndConsumeDefensiveCharge(double amount, double threshold) {
        if (amount <= 0 || threshold <= 0) return 0;
        defensiveReactionCharge += amount;
        int triggers = (int) (defensiveReactionCharge / threshold);
        defensiveReactionCharge -= triggers * threshold;
        return triggers;
    }

    public void setActionInterval(double actionInterval) {
        this.actionInterval = actionInterval;
        if (actionIntervalStat != null) actionIntervalStat.setBaseValue((float) actionInterval);
    }

    public void setAttackOffset(double attackOffset) {
        this.attackOffset = attackOffset;
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
        this.stackNumber++;
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

    public void instantlyMature() {
        if (this.growthTracker != null) {
            this.growthTracker.skipToMaxStage();
        }
    }

    public int getCurrentStage() {
        return growthTracker != null ? growthTracker.getCurrentStage() : 1;
    }

    public void setGrowthStage(int stage) {
        if (growthTracker != null) {
            growthTracker.setStage(stage);
        }
    }


    public double getIntervalTimer() {
        return internalTimer;
    }

    public void setInternalTimer(double timer) {
        this.internalTimer = timer;
    }

    public void setAbilityValue(double value) {
        this.abilityValue = value;
    }

    public List<Vec2> getShootingVectors() {
        return shootingVectors;
    }

    public void setShootingVectors(List<Vec2> shootingVectors) {
        this.shootingVectors = shootingVectors;
    }

    public void addShootingVectors(Vec2 vec2) {
        shootingVectors.add(vec2);
    }

    public boolean isPotatoMine() {
        return "Potato Mine".equalsIgnoreCase(name)
                || "Primal Potato Mine".equalsIgnoreCase(name);
    }

    public List<Vec2> getProjectileOrigins() {
        return projectileOrigins;
    }

    public void setProjectileOrigins(List<Vec2> projectileOrigins) {
        this.projectileOrigins = projectileOrigins != null
                ? new ArrayList<>(projectileOrigins)
                : new ArrayList<>();
    }

    public Vec2 getProjectileOrigin(int index) {
        if (projectileOrigins.isEmpty()) {
            return Vec2.of(0.5, 0.0);
        }

        int safeIndex = Math.max(0, Math.min(index, projectileOrigins.size() - 1));
        return projectileOrigins.get(safeIndex);
    }

    public void setState(PlantState state) {
        this.state = state;
    }

    public void beginDeathAnimation(float duration) {
        beginDeathAnimation(duration, duration, null);
    }

    public void beginDeathAnimation(float duration, float impactDelay, Runnable impactAction) {
        state = PlantState.DYING;
        deathAnimationTimer = Math.max(0.1f, duration);
        deathImpactTimer = Math.max(0f, Math.min(impactDelay, deathAnimationTimer));
        deathImpactAction = impactAction;
        animationController.playDying(deathAnimationTimer);
    }

    public PlantState getState() {
        return this.state;
    }

    public int getChillLevel() {
        return chillLevel;
    }

    public void setChillLevel(int chillLevel) {
        this.chillLevel = Math.max(0, Math.min(MAX_CHILL_LEVEL, chillLevel));
    }

    private double getChillIntervalMultiplier() {
        return switch (chillLevel) {
            case 1 -> CHILL_LEVEL_ONE_INTERVAL_MULTIPLIER;
            case 2 -> CHILL_LEVEL_TWO_INTERVAL_MULTIPLIER;
            default -> 1.0;
        };
    }

    public double getPlantFoodTimer() {
        return plantFoodTimer;
    }

    public void setPlantFoodTimer(double duration) {
        this.plantFoodTimer = duration;
    }

    public void setPlantFoodDuration(double plantFoodDuration) {
        this.plantFoodDuration = plantFoodDuration;
    }

    public boolean isPlantFoodIntroActive() {
        return plantFoodIntroActive;
    }

    public void setPlantFoodAnimReplays(int plantFoodAnimReplays) {
        this.plantFoodAnimReplays = Math.max(1, plantFoodAnimReplays);
    }

    public boolean onPlantFoodIntroClipFinished() {
        plantFoodIntroReplaysRemaining--;
        if (plantFoodIntroReplaysRemaining > 0) {
            return true;
        }
        plantFoodIntroActive = false;
        return false;
    }

    public int getStackNumber() {
        return this.stackNumber;
    }

    public boolean addPeaPodStack() {
        if (!"Pea Pod".equalsIgnoreCase(name)
                || stackNumber >= MAX_PEA_POD_STACK) {
            return false;
        }

        stackNumber++;
        shootingVectors.add(Vec2.of(1, 0));
        return true;
    }

    public void setArmor(PlantArmor armor) {
        this.armor = armor;
    }

    public PlantArmor getArmor() {
        return this.armor;
    }

    public boolean isBuffed() {
        return this.isBuffed;
    }

    public void setBuffed(boolean isBuffed) {
        this.isBuffed = isBuffed;
        if (isBuffed) {
            plantFoodIntroActive = true;
            plantFoodIntroReplaysRemaining = Math.max(1, plantFoodAnimReplays);
            if (this.plantFoodEffect != null) {
                plantFoodEffect.applyStatusModifiers(this);
                plantFoodEffect.triggerSuperpower(this, App.getGameSession());
            }
            if (this.plantFoodTimer <= 0.0) {
                this.plantFoodTimer = this.plantFoodDuration;
            }
        }
    }

    public boolean triggerActionAnimation() {
        return animationController.playAttack(name, getCurrentStage(), pamPath);
    }

    public boolean triggerGrowAnimation() {
        return animationController.playGrow(name, getCurrentStage(), pamPath);
    }

    public boolean triggerProduceAnimation() {
        return animationController.playProduce(name, pamPath);
    }

    public PlantAnimationController getAnimationController() {
        return animationController;
    }

    public void setLifetime(double lifetime) {
        this.lifetime = lifetime;
    }

    public void addSpecialUpgrade(SpecialUpgrade upgrade, double value) {
        if (upgrade == null) {
            return;
        }

        specialUpgrades.merge(upgrade, value, Double::sum);
    }

    public boolean hasSpecialUpgrade(SpecialUpgrade upgrade) {
        return specialUpgrades.containsKey(upgrade);
    }

    public double getSpecialUpgradeValue(SpecialUpgrade upgrade) {
        return specialUpgrades.getOrDefault(upgrade, 0.0);
    }

    public int getSpecialUpgradeInt(SpecialUpgrade upgrade) {
        return (int) Math.round(getSpecialUpgradeValue(upgrade));
    }

    public boolean consumeSmashCharge() {
        if (remainingSmashes < 0) {
            remainingSmashes = 1
                    + getSpecialUpgradeInt(SpecialUpgrade.BONUS_SMASH_CHARGES);
        }
        remainingSmashes--;
        return remainingSmashes > 0;
    }

    public boolean isImitationOverlayActive() {
        return imitationOverlayActive;
    }

    public void setImitationOverlayActive(boolean active) {
        this.imitationOverlayActive = active;
    }

    public String getImitationTargetName() {
        return imitationTargetName;
    }

    public void setImitationTargetName(String imitationTargetName) {
        this.imitationTargetName = imitationTargetName;
    }

    public void setImitationSourcePlant(Plant imitationSourcePlant) {
    }

    public boolean consumeJustTransformed() {
        boolean value = justTransformed;
        justTransformed = false;
        return value;
    }

    public void beginImitation(Plant target) {
        if (actStrategy instanceof ImitaterStrategy imitater) {
            imitater.beginImitation(this, target);
        }
    }

    public void onImitateIdleClipFinished() {
        if (actStrategy instanceof ImitaterStrategy imitater) {
            imitater.onIdleClipFinished(this);
        }
    }

    public void onImitateAttackClipFinished() {
        if (actStrategy instanceof ImitaterStrategy imitater) {
            imitater.onAttackClipFinished(this);
        }
    }

    public void transformInto(String targetPlantName) {
        Plant copy = PlantFactory.createPlantByName(targetPlantName, 1);
        copyFrom(copy);
        this.animationController.playIdle();
        this.state = PlantState.ACTIVE;
        this.justTransformed = true;
        this.imitationOverlayActive = false;
    }

    private void copyFrom(Plant copy) {
        PlantStateCopier.copy(this, copy);
    }

    private boolean isIndestructibleIceTrap() {
        return this.getTags().contains(Tag.ICE)
                && this.getTags().contains(Tag.TRAP)
                && this.getDamage() <= 0;
    }

    public boolean isMineArmed(){
        return this.mineArmed;
    }

    public float getMineRecoverTimer() {
        return this.mineRecoverTimer;
    }
    public void updateInternalTimer() {
        if (actStrategy instanceof MeleeStrategy
                || actStrategy instanceof ShockwaveStrategy) {
            internalTimer = actionInterval;
        } else if (actStrategy instanceof SunProduceStrategy) {
            internalTimer = actionInterval / 2.0;
        } else {
            internalTimer = 0.0;
        }
    }
}