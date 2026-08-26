package com.ussr.pvz.view.mainmenu.greenhouse;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.controller.maincontroller.GreenHouseController;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.Map;

public class PotWidget extends Table {

    public static final String STATIC_POT_REGION = "IMAGE_ZEN_GARDEN_GROWING_PLANT_SLOT_GROWING_PLANT_SLOT_184X161";
    public static final String PAM_POT_PATH = "768/INITIAL/ZEN_GARDEN/GROWING_PLANT_SLOT/GROWING_PLANT_SLOT.PAM";
    // Cost to instantly finish a watered/growing sprout. Must match
    // Greenhouse.GEMS_PER_HOUR — kept here only for the button's label text.
    private static final int GEMS_PER_HOUR = 4;

    private final Skin skin;
    private final PamPlayer pamPlayer;
    private final TextureBank textures;
    private final GreenHouseController controller;
    private final Runnable onStateChanged;

    public PotWidget(int x, int y, Map<String, Object> potMap, Skin skin, PamPlayer pamPlayer,
                     TextureBank textures, GreenHouseController controller, Runnable onStateChanged) {
        this.skin = skin;
        this.pamPlayer = pamPlayer;
        this.textures = textures;
        this.controller = controller;
        this.onStateChanged = onStateChanged;

        // Force Table to align from TOP-CENTER so height changes in buttons don't shift the pot
        top().center();

        buildUI(x, y, potMap);
    }

    @SuppressWarnings("unchecked")
    private void buildUI(int x, int y, Map<String, Object> potMap) {
        clearChildren();

        boolean unlocked = potMap != null && Boolean.TRUE.equals(potMap.get("unlocked"));
        boolean occupied = potMap != null && Boolean.TRUE.equals(potMap.get("occupied"));

        // ----------------------------------------------------
        // 1. LOCKED SLOT
        // ----------------------------------------------------
        if (!unlocked) {
            TextButton unlockButton = new TextButton("Unlock\n[20 Gems]", skin, "brown");
            unlockButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String msg = controller.handleUnlock(x, y);
                    if (msg.contains("unlocked")) {
                        NotificationCenter.success(msg);
                    } else {
                        NotificationCenter.error(msg);
                    }
                    onStateChanged.run();
                }
            });
            add(unlockButton).width(100f).height(48f).padTop(20f);
            return;
        }

        // Standardized Stack Container (Rigid 80x70 bounding box)
        Group potStack = new Group();
        potStack.setSize(80f, 70f);

        // ----------------------------------------------------
        // 2. UNLOCKED & EMPTY (Render PAM Pot)
        // ----------------------------------------------------
        if (!occupied || potMap == null || !potMap.containsKey("plant")) {
            PamActor pamPot = new PamActor(pamPlayer, PAM_POT_PATH, "idle");
            pamPot.setSize(80f, 70f);
            pamPot.setPamScale(0.36f);
            pamPot.setOffsetY(-12f); // Align origin with static pot base
            potStack.addActor(pamPot);

            // Force exact row height in Table
            add(potStack).size(80f, 70f).row();

            TextButton plantButton = new TextButton("Plant", skin, "green");
            plantButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String msg = controller.handlePlant(x, y);
                    NotificationCenter.info(msg);
                    onStateChanged.run();
                }
            });
            add(plantButton).width(85f).height(32f).padTop(2f);
            return;
        }

        // ----------------------------------------------------
        // 3. PLANTED / OCCUPIED (Render Static Atlas Pot + Sprout)
        // ----------------------------------------------------

        TextureRegion staticPotRegion = textures != null ? textures.region(STATIC_POT_REGION) : null;
        if (staticPotRegion != null) {
            Image staticPot = new Image(staticPotRegion);
            staticPot.setSize(80f, 70f);
            staticPot.setPosition(0, 0);
            potStack.addActor(staticPot);
        }

        // Sprout PAM centered directly on top of the soil
        // Inside PotWidget.java under section 3:

        Map<String, Object> plantMap = (Map<String, Object>) potMap.get("plant");

        String plantType = "SPROUT";
        if (plantMap != null) {
            if (plantMap.containsKey("type")) {
                plantType = String.valueOf(plantMap.get("type"));
            } else if (plantMap.containsKey("plantType")) {
                plantType = String.valueOf(plantMap.get("plantType"));
            } else if (plantMap.containsKey("species")) {
                plantType = String.valueOf(plantMap.get("species"));
            }
        }

        SproutView sproutView = new SproutView(pamPlayer, plantType);
        potStack.addActor(sproutView);
        sproutView.setSize(80f, 70f);
        sproutView.setPamScale(0.35f);
        sproutView.setOffsetY(8f); // Soil line offset
        potStack.addActor(sproutView);

        // Force exact row height in Table
        add(potStack).size(80f, 70f).row();

        // ----------------------------------------------------
        // 4. ACTION BUTTONS (Water / Collect)
        // ----------------------------------------------------
        assert plantMap != null;
        boolean isUnwatered = "UNWATERED".equals(plantMap.get("state"));
        long plantedAt = ((Number) plantMap.getOrDefault("plantedAtMillis", 0L)).longValue();
        long duration = ((Number) plantMap.getOrDefault("growthDurationMillis", 0L)).longValue();
        long finishTime = plantedAt + duration;
        boolean isReady = !isUnwatered
                && (System.currentTimeMillis() >= finishTime || "READY".equals(plantMap.get("state")));

        if (isReady) {
            TextButton collectButton = new TextButton("Collect!", skin, "green");
            collectButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String msg = controller.handleCollect(x, y);
                    NotificationCenter.success(msg);
                    onStateChanged.run();
                }
            });
            add(collectButton).width(85f).height(32f).padTop(2f);
        } else if (isUnwatered) {
            TextButton waterButton = new TextButton("Water", skin, "brown");
            waterButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Stage stage = getStage();
                    Runnable doWater = () -> {
                        String msg = controller.handleWater(x, y);
                        NotificationCenter.info(msg);
                        onStateChanged.run();
                    };
                    if (stage != null) {
                        float waterX = getX() + 25f;
                        float waterY = getY() + 30f;
                        WateringEffectActor waterAnim =
                                new WateringEffectActor(pamPlayer, waterX, waterY, doWater);
                        stage.addActor(waterAnim);
                    } else {
                        doWater.run();
                    }
                }
            });
            add(waterButton).width(105f).height(32f).padTop(2f);
        } else {
            // Watered and growing: offer to skip the remaining time for gems.
            long remainingMillis = Math.max(0, finishTime - System.currentTimeMillis());
            long remainingHours = (remainingMillis + 3599999) / 3600000;
            long gemCost = remainingHours * GEMS_PER_HOUR;

            TextButton speedBtn = new TextButton(
                    remainingHours + "h [" + gemCost + " Gems]", skin, "brown");
            speedBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String msg = controller.handleGrow(x, y);
                    NotificationCenter.info(msg);
                    onStateChanged.run();
                }
            });
            add(speedBtn).width(105f).height(32f).padTop(2f);
        }
    }
}