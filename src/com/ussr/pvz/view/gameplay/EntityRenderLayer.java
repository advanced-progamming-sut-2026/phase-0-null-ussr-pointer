package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.items.GroundItem;
import com.ussr.pvz.model.entities.items.ItemType;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.board.structures.LawnMower;
import com.ussr.pvz.model.entities.projectiles.Projectile;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the immediate-mode rendering of all simulation entities.
 * Maps grid coordinates (x, y) to absolute screen pixels.
 */
public class EntityRenderLayer extends Actor {

    // Calibration offsets to align the 9x5 grid onto the visual background
    private static final float GRID_OFFSET_X = 260f;
    private static final float GRID_OFFSET_Y = 110f;
    private static final float CELL_WIDTH = 80f;
    private static final float CELL_HEIGHT = 100f;

    private final PamPlayer pamPlayer;
    private final TextureBank textures;

    public EntityRenderLayer(PamPlayer pamPlayer, TextureBank textures) {
        this.pamPlayer = pamPlayer;
        this.textures = textures;
        setTouchable(Touchable.disabled); // Clicks must pass through to the LawnWidget
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        GameSession session = App.getGameSession();
        if (session == null) return;

        // Render order dictates Z-indexing (Painter's Algorithm)
        renderMowers(batch, session);

        int rows = session.getLawn().getRows();
        // Render from top row (4) to bottom row (0) for proper depth overlap
        for (int r = rows - 1; r >= 0; r--) {
            renderRowEntities(batch, session, r);
        }

        renderProjectiles(batch, session);
        renderDrops(batch, session);
    }

    private void renderRowEntities(Batch batch, GameSession session, int row) {
        for (Plant plant : session.getPlants()) {
            if (plant.isAlive() && plant.getLocation().y() == row) {
                renderPlant(batch, plant, session.getElapsedSeconds());
            }
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive() && zombie.getPosition().y() == row) {
                renderZombie(batch, zombie, session.getElapsedSeconds());
            }
        }
    }

    private void renderMowers(Batch batch, GameSession session) {
        for (LawnMower mower : session.getLawnMowers()) {
            if (!mower.isAlive()) continue;

            float screenX = GRID_OFFSET_X + (float)(mower.getPosition().x() * CELL_WIDTH) - 40f;
            float screenY = GRID_OFFSET_Y + (float)(mower.getPosition().y() * CELL_HEIGHT);

            try {
                pamPlayer.draw(batch, "768/INITIAL/LAWNMOWER/LAWNMOWER.PAM", "animation",
                        (float) session.getElapsedSeconds(), screenX, screenY, true);
            } catch (Exception ignored) {
                // Fallback to static texture if PAM is missing
                TextureRegion region = textures.region("IMAGE_LAWNMOWER");
                if (region != null) batch.draw(region, screenX, screenY, 64, 64);
            }
        }
    }

    private void renderPlant(Batch batch, Plant plant, double stateTime) {
        float screenX = GRID_OFFSET_X + (plant.getLocation().x() * CELL_WIDTH);
        float screenY = GRID_OFFSET_Y + (plant.getLocation().y() * CELL_HEIGHT);

        String sanitizedName = plant.getName().toUpperCase().replaceAll("[\\s_\\-]", "");
        String pamPath = "768/INITIAL/PLANTS/" + sanitizedName + "/" + sanitizedName + ".PAM";

        String clip = plant.getPlantFoodTimer() > 0 ? "plantfood" : "idle";

        try {
            pamPlayer.draw(batch, pamPath, clip, (float) stateTime, screenX + 40f, screenY + 20f, true);
        } catch (Exception ignored) { }
    }

    private void renderZombie(Batch batch, Zombie zombie, double stateTime) {
        float screenX = GRID_OFFSET_X + (float)(zombie.getPosition().x() * CELL_WIDTH);
        float screenY = GRID_OFFSET_Y + (float)(zombie.getPosition().y() * CELL_HEIGHT);

        String pamPath = getZombiePamPath(zombie.getAlias());

        // Default to walking visually
        String clip = "walk";

        Map<String, Boolean> visibilityMap = buildZombieArmorVisibilityMap(zombie);

        try {
            // Apply armor modifiers visually via alias mapping[cite: 5]
            pamPlayer.draw(batch, pamPath, clip, (float) stateTime, screenX + 40f, screenY + 40f, true, visibilityMap);
        } catch (Exception ignored) { }
    }

    private String getZombiePamPath(String alias) {
        if (alias.contains("Armor") || alias.equalsIgnoreCase("ZombieDefault")) {
            return "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM";
        }
        String sanitizedName = alias.toUpperCase().replaceAll("[\\s_]", "");
        return "768/INITIAL/ZOMBIE/" + sanitizedName + "/" + sanitizedName + ".PAM";
    }

    private Map<String, Boolean> buildZombieArmorVisibilityMap(Zombie zombie) {
        Map<String, Boolean> vis = new HashMap<>();
        String alias = zombie.getAlias();

        vis.put("zombie_armor_cone_norm", false);
        vis.put("zombie_armor_bucket_norm", false);
        vis.put("zombie_armor_brick_norm", false);

        // Armor mapping defined statically via aliases
        switch (alias) {
            case "ZombieArmor1" -> vis.put("zombie_armor_cone_norm", true);
            case "ZombieArmor2" -> vis.put("zombie_armor_bucket_norm", true);
            case "ZombieArmor4" -> vis.put("zombie_armor_brick_norm", true);
        }

        return vis;
    }

    private void renderProjectiles(Batch batch, GameSession session) {
        for (Projectile proj : session.getProjectiles()) {
            if (!proj.isAlive()) continue;

            float screenX = GRID_OFFSET_X + (float)(proj.getPosition().x() * CELL_WIDTH);
            float screenY = GRID_OFFSET_Y + (float)(proj.getPosition().y() * CELL_HEIGHT);

            TextureRegion region = textures.region("IMAGE_EFFECTS_T_PEA_PROJECTILE_T_PEA_PROJECTILE_39X36");
            if (region != null) {
                batch.draw(region, screenX, screenY + 40f, 20f, 20f);
            }
        }
    }

    private void renderDrops(Batch batch, GameSession session) {
        for (GroundItem item : session.getItems()) {
            if (item.isCollected()) continue;

            float screenX = GRID_OFFSET_X + (float)(item.getPosition().x() * CELL_WIDTH);
            float screenY = GRID_OFFSET_Y + (float)(item.getPosition().y() * CELL_HEIGHT);

            if (item.getItemType() == ItemType.SUN) {
                try {
                    pamPlayer.draw(batch, "768/INITIAL/EFFECTS/SUN/SUN.PAM", "idle",
                            (float) session.getElapsedSeconds(), screenX, screenY, true);
                } catch (Exception ignored) {
                    TextureRegion region = textures.region("IMAGE_DANGERROOM_CARD_SUN");
                    if (region != null) batch.draw(region, screenX, screenY, 48f, 48f);
                }
            } else if (item.getItemType() == ItemType.PLANT_FOOD) {
                TextureRegion region = textures.region("IMAGE_PLANTFOOD");
                if (region != null) batch.draw(region, screenX, screenY, 48f, 48f);
            }
        }
    }
}