package com.ussr.pvz.model.entities.plants;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.Damageable;
import com.ussr.pvz.model.entities.plants.animation.PlantAnimationController;
import com.ussr.pvz.model.engine.GameEntity;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.modifiers.ModifiableStat;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.plants.actstrategy.ActStrategy;
import com.ussr.pvz.model.entities.plants.actstrategy.WallNutStrategy;
import com.ussr.pvz.model.entities.plants.actstrategy.MeleeStrategy;
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
    private int maxHp;
    private double maxRecharge;
    private double recharge;
    private double actionInterval;
    private double attackOffset = -1.0;
    private int cost;
    private Location location;
    private final ArrayList<Tag> tags = new ArrayList<>();
    private int damage;
    private PlantType type;
    private Plant bottom = null;
    private int stackNumber = 1;
    private boolean isBuffed = false;

    private ModifiableStat hpStat;
    private ModifiableStat actionIntervalStat;

    private ActStrategy actStrategy;
    private PlantFoodEffect plantFoodEffect;
    private PlantFoodType plantFoodType;
    private double internalTimer = 0.0;
    private double abilityValue;
    private int chillLevel = 0;
    private GrowthTracker growthTracker;
    private List<Vec2> projectileOrigins = new ArrayList<>();
    private String pamPath;
    private String projectilePam;
    private String hitPam;
    private String plantFoodProjectilePam;
    private String plantFoodHitPam;
    // Animation State Manager Variables
    private final PlantAnimationController animationController =
            new PlantAnimationController();
    private double plantFoodTimer = 0.0;
    // True while Cactus's one-shot "plantfood" transform clip is still
    // playing. Set when plant food is applied; cleared by the render layer
    // once it observes the real PAM clip has finished (see
    // EntityRenderLayer#syncPlants / PamActor#isPlaying), not a guessed
    // duration, since the actual clip length comes from the PAM asset itself.
    private boolean plantFoodIntroActive = false;

    private PlantArmor armor;

    private double lifetime = Double.MAX_VALUE;
    private int remainingSmashes = -1;

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

    public enum PlantState {
        ACTIVE,
        INCAPACITATED,
        PREPPING,
        DYING
    }

    private PlantState state;
    private float deathAnimationTimer;
    private float deathImpactTimer;
    private Runnable deathImpactAction;
    private float pendingAttackTimer;
    private Runnable pendingAttackAction;
    private boolean mineArmed;
    private float mineRecoverTimer;
    private double defensiveReactionCharge;

    private final EnumMap<SpecialUpgrade, Double> specialUpgrades =
            new EnumMap<>(SpecialUpgrade.class);
    private List<Vec2> shootingVectors = new ArrayList<>();

    public Plant() {
    }

    public Plant(Plant blueprint) {
        this.id = blueprint.id;
        this.name = blueprint.name;
        this.level = blueprint.level;
        this.type = blueprint.type;
        this.tags.addAll(blueprint.tags);
        this.specialUpgrades.putAll(blueprint.specialUpgrades);
        this.state = PlantState.ACTIVE;
        this.location = blueprint.location;
        this.actStrategy = blueprint.actStrategy;
        this.plantFoodEffect = blueprint.plantFoodEffect;
        this.setWrampUp(blueprint.getWrampUp());
        this.plantFoodType = blueprint.plantFoodType;
        this.shootingVectors = new ArrayList<>(blueprint.shootingVectors);
        this.projectileOrigins = new ArrayList<>(blueprint.projectileOrigins);

        this.hp = blueprint.hp;
        this.maxHp = blueprint.maxHp;
        this.cost = blueprint.cost;
        this.damage = blueprint.damage;
        this.actionInterval = blueprint.actionInterval;
        this.attackOffset = blueprint.attackOffset;
        this.recharge = blueprint.recharge;
        this.maxRecharge = blueprint.maxRecharge;
        this.abilityValue = blueprint.abilityValue;
        this.lifetime = blueprint.lifetime;
        this.remainingSmashes = blueprint.remainingSmashes;

        this.hpStat = new ModifiableStat(this.hp);
        this.actionIntervalStat = new ModifiableStat((float) this.actionInterval);
        this.actStrategy = blueprint.actStrategy;
        this.plantFoodTimer = blueprint.plantFoodTimer;
        this.armor = blueprint.armor;
        this.plantFoodEffect = blueprint.plantFoodEffect;
        this.projectilePam = blueprint.projectilePam;
        this.hitPam = blueprint.hitPam;
        this.plantFoodHitPam = blueprint.plantFoodHitPam;
        this.plantFoodProjectilePam = blueprint.plantFoodProjectilePam;
        this.growthTracker = blueprint.growthTracker;
        this.pamPath = blueprint.pamPath;
        if (this.actStrategy instanceof MeleeStrategy) {
            this.internalTimer = this.actionInterval;
        } else {
            this.internalTimer = 0.0;
        }
    }

    @Override
    public void update(float delta) {
        if (!isAlive && state != PlantState.DYING) {
            return; // Allow DYING state to process for explosive animations
        }
        animationController.update(delta);

        if (state == PlantState.DYING) {
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
            return;
        }

        if (state == PlantState.INCAPACITATED) {
            animationController.playIncapacitated();
            return;
        }

        if (pendingAttackAction != null) {
            pendingAttackTimer -= delta;
            if (pendingAttackTimer <= 0f) {
                Runnable attack = pendingAttackAction;
                pendingAttackAction = null;
                attack.run();
            }
        }

        lifetime -= delta;
        if (lifetime < 0) {
            killPlant();
            return;
        }

        updateStats(delta);
        updatePlantFood(delta);
        updateGrowth(delta);
        if (isBuffed || actStrategy == null) {
            return;
        }

        updateAction(delta);
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
        if (hpStat != null) {
            hpStat.update(delta);
        }

        if (actionIntervalStat != null) {
            actionIntervalStat.update(delta);
        }

    }

    private void updatePlantFood(float delta) {
        if (!isBuffed) {
            return;
        }

        plantFoodTimer -= delta;

        if (plantFoodTimer <= 0f) {
            isBuffed = false;
            // Safety cleanup: if the buff somehow ends before the render
            // layer told us the intro clip finished (e.g. a very short
            // plant food duration), don't leave the plant stuck.
            plantFoodIntroActive = false;
            return;
        }

        if (plantFoodEffect != null) {
            plantFoodEffect.tickDurationEffect(
                    this,
                    App.getGameSession(),
                    delta
            );
        }
    }

    private void updateAction(float delta) {
        internalTimer += delta;

        double interval = getActionInterval() * getChillIntervalMultiplier();

        if (isPotatoMine()) {
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

            // Once armed, mines must test their trigger radius every update;
            // actionInterval is their one-time arming duration, not a cooldown.
            actStrategy.act(this, App.getGameSession());
            return;
        }

        if (internalTimer >= interval) {
            actStrategy.act(this, App.getGameSession());
            internalTimer -= interval;
        }
    }

    @Override
    public void takeDamage(int damage) {
        takeDamage(damage, null);
    }

    public void takeDamage(int damage, Zombie dealer) {
//        System.out.println("o yeah");
//        if(dealer != null)
//            System.out.println("x : " + dealer.getPosition().x() + " y : " + dealer.getPosition().y());
        if (!isAlive || state == PlantState.DYING) return;

        int remainingDamage = damage;

        if (this.armor != null && !this.armor.isDestroyed()) {
            remainingDamage = this.armor.absorbDamage(remainingDamage, this);
            this.armor.handleReflection(dealer, this);

            if (this.armor.isDestroyed()) {
                this.armor = null;
            }
        }


        if (remainingDamage > 0) {
            if (this.actStrategy instanceof WallNutStrategy strategy) {
                strategy.onDamageAct(this, dealer, Math.min(getHp(), remainingDamage));
            }
            int newHp = getHp() - remainingDamage;
            if (newHp <= 0) {
                if (name.equalsIgnoreCase("Hypno-shroom")) {
                    if (dealer instanceof Zombie zombie) {
                        double hpMultiplier = getSpecialUpgradeValue(
                                SpecialUpgrade.ZOMBIE_HEALTH_MULTIPLIER);
                        double damageMultiplier = getSpecialUpgradeValue(
                                SpecialUpgrade.ZOMBIE_DAMAGE_MULTIPLIER);
                        if (hpMultiplier > 0) {
                            zombie.setMaxHp((int) Math.round(zombie.getMaxHp() * hpMultiplier));
                            zombie.setHp((int) Math.round(zombie.getHp() * hpMultiplier));
                        }
                        if (damageMultiplier > 0) {
                            zombie.setEatDps(zombie.getEatDps() * damageMultiplier);
                        }
                        zombie.hypnotize();
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
            } else {
                setHp(newHp);
            }
        }
    }

    public void updateGrowth(double deltaTimeSeconds) {
        if (growthTracker != null) {
            double reduction = getSpecialUpgradeValue(SpecialUpgrade.GROW_TIME_REDUCTION);
            double speedMultiplier = reduction < 0 ? 1.0 + (-reduction / 24.0) : 1.0;
            growthTracker.update(deltaTimeSeconds * speedMultiplier);
        }
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

    public double getAttackOffset() {
        return attackOffset;
    }

    public void setAttackOffset(double attackOffset) {
        this.attackOffset = attackOffset;
    }

    public float getAttackDelay(float defaultSeconds) {
        return attackOffset >= 0.0
                ? (float) attackOffset
                : defaultSeconds;
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

    public record Location(int x, int y) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Plant.Location(int x1, int y1))) return false;
            return x == x1 && y == y1;
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

    private boolean isPotatoMine() {
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

    public boolean isPlantFoodIntroActive() {
        return plantFoodIntroActive;
    }

    public void setPlantFoodIntroActive(boolean plantFoodIntroActive) {
        this.plantFoodIntroActive = plantFoodIntroActive;
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
            if ("Cactus".equalsIgnoreCase(name)) {
                plantFoodIntroActive = true;
            }
            if (this.plantFoodEffect != null) {
                plantFoodEffect.applyStatusModifiers(this);
                plantFoodEffect.triggerSuperpower(this, App.getGameSession());
            }
        }
    }

    public void triggerActionAnimation(float duration) {
        animationController.playAttack(name, getCurrentStage(), duration);
    }

    public void scheduleAttack(float delay, Runnable action) {
        if (action == null) {
            return;
        }
        if (delay <= 0f) {
            action.run();
            return;
        }
        pendingAttackTimer = delay;
        pendingAttackAction = action;
    }

    public void triggerProduceAnimation(float duration) {
        animationController.playProduce(name, duration);
    }

    public PlantAnimationController getAnimationController() {
        return animationController;
    }
    /**
     * Evaluates the plant's current situation and returns the correct PAM clip.
     */
    public String getAnimationClip() {
        // 1. Explosive / Dying State (e.g., Cherry Bomb)
        if (state == PlantState.DYING) {
            if ("Doom-shroom".equalsIgnoreCase(name)) {
                int stage = Math.max(1, Math.min(3, getCurrentStage()));
                return "stage" + stage + "_explode";
            }
            if ("Squash".equalsIgnoreCase(name)) {
                return "jump_down_left";
            }
            if ("Explode-o-nut".equalsIgnoreCase(name)) {
                return "damage3";
            }
            return "attack";
        }

        // 2. Plant Food State
        if (plantFoodTimer > 0 || isBuffed) {
            if ("Cactus".equalsIgnoreCase(name)) {
                if (plantFoodIntroActive) {
                    return "plantfood";
                }
                return animationController.getCurrentClip() + "_plantfood";
            }
            return "plantfood"; // Note: If your PamActor uses "plantfood_on" as a fallback, handle that inside PlantPamActor.
        }

        // 3. Prepping State (Mints intro, or Potato Mine charging)
        if (state == PlantState.PREPPING) {
            // According to image_24af05.jpg, Mints have an intro and Charge plants have an unready animation.
            // You may need to change "prepping" to the exact PAM string (like "intro" or "unarmed").
            return "prepping";
        }

        // 4. Defensive Plants Damage States (Wall-nut, Tall-nut)
        // According to image_24af05.jpg, defensive plants change appearance 2 to 3 times as health decreases.
        if (this.actStrategy instanceof WallNutStrategy) {
            float hpPercent = (float) getHp() / (float) getMaxHp();

            if ("Sweet Potato".equalsIgnoreCase(name)) {
                if (hpPercent <= 0.15f) return "idle_damage3";
                if (hpPercent <= 0.40f) return "idle_damage2";
                if (hpPercent <= 0.70f) return "idle_damage";
            } else if ("Wall-nut".equalsIgnoreCase(name)
                    || "Explode-o-nut".equalsIgnoreCase(name)
                    || "Endurian".equalsIgnoreCase(name)) {
                if (hpPercent <= 0.15f) return "damage3";
                if (hpPercent <= 0.40f) return "damage2";
                if (hpPercent <= 0.70f) return "damage";
            } else if ("Tall-nut".equalsIgnoreCase(name)) {
                if (hpPercent <= 0.40f) return "damage2";
                if (hpPercent <= 0.70f) return "damage";
            } else if ("Garlic".equalsIgnoreCase(name)) {
                if (hpPercent <= 0.40f) return "idle_damage2";
                if (hpPercent <= 0.70f) return "idle_damage";
            } else if ("Pumpkin".equalsIgnoreCase(name)) {
                if (hpPercent <= 0.40f) return "idle3";
                if (hpPercent <= 0.70f) return "idle2";
            }
        }

        if (isPotatoMine()) {
            if (!mineArmed) return "plant_idle";
            if (mineRecoverTimer > 0f) return "recover";
            return "idle";
        }

        // 5. Default Action or Idle State
        String currentClip = animationController.getCurrentClip();
        if ("Pea Pod".equalsIgnoreCase(name)) {
            int visiblePods = Math.max(1, Math.min(MAX_PEA_POD_STACK, stackNumber));
            if ("idle".equals(currentClip)) {
                return visiblePods == 1 ? "idle" : "idle" + visiblePods;
            }
            if ("attack".equals(currentClip)) {
                return visiblePods == 1 ? "attack" : "attack " + visiblePods;
            }
        }
        return currentClip;
    }

    public void setLifetime(double lifetime) {
        this.lifetime = lifetime;
    }

    public double getLifetime() {
        return lifetime;
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

    public Map<SpecialUpgrade, Double> getSpecialUpgrades() {
        return Map.copyOf(specialUpgrades);
    }

    public boolean consumeSmashCharge() {
        if (remainingSmashes < 0) {
            remainingSmashes = 1
                    + getSpecialUpgradeInt(SpecialUpgrade.BONUS_SMASH_CHARGES);
        }
        remainingSmashes--;
        return remainingSmashes > 0;
    }
}