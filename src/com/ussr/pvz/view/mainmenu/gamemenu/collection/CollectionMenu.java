package com.ussr.pvz.view.mainmenu.gamemenu.collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.service.CollectionService;
import com.ussr.pvz.service.CollectionService.PlantData;
import com.ussr.pvz.service.CollectionService.ZombieData;
import com.ussr.pvz.view.FadingMenu;
import com.ussr.pvz.view.animation.PamActor;
import com.ussr.pvz.view.animation.ZombiePamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

public class CollectionMenu extends FadingMenu {
    private final Skin skin;
    private final CollectionService collectionService;
    private final TextureBank textures;
    private final PamPlayer pamPlayer;

    private Table contentTable;
    private ScrollPane scrollPane;
    private Table mainLayout;
    private TextureRegionDrawable dimBackground;

    public CollectionMenu(Skin skin) {
        this.skin = skin;
        this.collectionService = new CollectionService();
        this.textures = new TextureBank("ATLASES", Gdx.files.local("pvz-assets"));
        this.pamPlayer = new PamPlayer(textures, Gdx.files.local("pvz-assets"));
        createDimBackground();
        buildUI();
    }

    private void createDimBackground() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.85f));
        pixmap.fill();
        dimBackground = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
    }

    private void buildUI() {
        mainLayout = new Table();
        mainLayout.setFillParent(true);

        TextureRegion bgRegion = textures.region("image_ui_quests_travel_log_final");
        if (bgRegion == null) bgRegion = textures.region("image_ui_quests_travel_log_corner");
        if (bgRegion != null) mainLayout.setBackground(new TextureRegionDrawable(bgRegion));

        Table tabsTable = new Table();
        TextButton btnPlants = new TextButton("Plants", skin, "green");
        TextButton btnZombies = new TextButton("Zombies", skin, "purple");

        tabsTable.add(btnPlants).pad(10).width(200);
        tabsTable.add(btnZombies).pad(10).width(200);

        contentTable = new Table();
        contentTable.top().pad(20);

        scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        mainLayout.add(tabsTable).top().expandX().fillX().padTop(10).row();
        mainLayout.add(scrollPane).expand().fill().pad(20);

        this.addActor(mainLayout);

        btnPlants.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { loadPlantsTab(); }
        });
        btnZombies.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { loadZombiesTab(); }
        });

        loadPlantsTab();
    }

    private void loadPlantsTab() {
        contentTable.clearChildren();
        List<PlantData> plants = collectionService.getPlantDataForGUI();

        int columns = 6;
        int currentCount = 0;

        for (PlantData plant : plants) {
            Table card = new Table();
            card.setTouchable(Touchable.enabled);

            TextureRegion cardBg = textures.region("image_ui_cards_almanac_plant_card");
            if (cardBg != null) {
                card.setBackground(new TextureRegionDrawable(cardBg));
            } else if (skin.has("image_ui_cards_almanac_plant_card_10", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
                card.setBackground(skin.getDrawable("image_ui_cards_almanac_plant_card_10"));
            }

            PamActor pamActor = new PamActor(pamPlayer, plant.pamPath, "idle");
            pamActor.setPamScale(0.3f);
            card.add(pamActor).size(80, 80).padTop(10).row();

            Label statusLbl = new Label(plant.level > 0 ? "Lvl " + plant.level : "Locked", skin, "secondary");
            statusLbl.setAlignment(Align.center);
            card.add(statusLbl).bottom().pad(5);

            if (plant.level == 0) {
                card.setColor(Color.DARK_GRAY);
            } else {
                card.setColor(Color.WHITE);
            }

            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showPlantDetailsOverlay(plant);
                }
            });

            contentTable.add(card).size(110, 160).pad(10);
            currentCount++;
            if (currentCount >= columns) {
                contentTable.row();
                currentCount = 0;
            }
        }
    }

    private void loadZombiesTab() {
        contentTable.clearChildren();
        List<ZombieData> zombies = collectionService.getZombieDataForGUI();

        int columns = 6;
        int currentCount = 0;

        for (ZombieData zombie : zombies) {
            Table card = new Table();
            card.setTouchable(Touchable.enabled);

            // Fetch lowercase name for the frame
            TextureRegion cardBg = textures.region("image_ui_cards_almanac_zombie_card");
            if (cardBg != null) {
                card.setBackground(new TextureRegionDrawable(cardBg));
            }

            // Create a Stack to layer the background tile and the zombie animation
            Stack animStack = new Stack();

            // Add the floor background tile
            TextureRegion floorTile = textures.region("image_ui_dialog_asset_dialogtexture");
            if (floorTile != null) {
                Image floorImage = new Image(new TextureRegionDrawable(floorTile));
                // Optional: Adjust scaling if needed to fit nicely in the card
                animStack.add(floorImage);
            }

            // Add the Zombie PAM Animation
            ZombiePamActor pamActor = new ZombiePamActor(pamPlayer, zombie.pamPath);
            // Scale down specifically for the grid view
            pamActor.setPamScale(0.3f);
            // Adjust offset for the grid scale
            pamActor.setOffsetY(-15f);
            animStack.add(pamActor);

            card.add(animStack).size(80, 80).padTop(10).row();

            Label nameLbl = new Label(zombie.name, skin, "default");
            nameLbl.setFontScale(0.65f);
            nameLbl.setAlignment(Align.center);
            card.add(nameLbl).bottom().pad(5);

            // Apply the Bright / Dim logic to the entire card
            if (zombie.encountered) {
                card.setColor(Color.WHITE); // Bright
            } else {
                card.setColor(Color.DARK_GRAY); // Dim
            }

            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showZombieDetailsOverlay(zombie);
                }
            });

            contentTable.add(card).size(110, 160).pad(10);
            currentCount++;
            if (currentCount >= columns) {
                contentTable.row();
                currentCount = 0;
            }
        }
    }

    private void showPlantDetailsOverlay(PlantData plant) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = skin.get("default", Label.LabelStyle.class).font;
        if (skin.has("image_ui_dialog_asset_dialogborder", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            dialogStyle.background = skin.getDrawable("image_ui_dialog_asset_dialogborder");
        }
        dialogStyle.stageBackground = dimBackground;

        Dialog dialog = new Dialog("", dialogStyle);
        Table content = dialog.getContentTable();

        Table leftSide = new Table();
        Table rightSide = new Table();

        PamActor pamActor = new PamActor(pamPlayer, plant.pamPath, "idle");
        pamActor.setPamScale(0.8f);
        leftSide.add(pamActor).size(200, 200);

        rightSide.add(new Label(plant.name, skin, "big_outline")).left().padBottom(15).row();
        rightSide.add(new Label("Level: " + (plant.level > 0 ? plant.level : "Not Owned"), skin, "medium")).left().row();

        Table statsTable = new Table();
        statsTable.add(new Label("Sun Cost:\n" + plant.cost, skin, "secondary")).padRight(20);
        statsTable.add(new Label("Recharge:\n" + plant.recharge + "s", skin, "secondary")).padRight(20);
        statsTable.add(new Label("Toughness:\n" + plant.baseHp, skin, "secondary"));
        rightSide.add(statsTable).left().padTop(10).row();

        rightSide.add(new Label("Damage: " + plant.damage, skin, "secondary")).left().padTop(10).row();

        content.add(leftSide).pad(20);
        content.add(rightSide).pad(20).top().left();

        dialog.getButtonTable().pad(20);
        TextButton closeBtn = new TextButton("Close", skin, "brown");
        dialog.button(closeBtn, true);

        dialog.show(getStage());
    }

    private void showZombieDetailsOverlay(ZombieData zombie) {
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = skin.get("default", Label.LabelStyle.class).font;
        if (skin.has("image_ui_dialog_asset_dialogborder", com.badlogic.gdx.scenes.scene2d.utils.Drawable.class)) {
            dialogStyle.background = skin.getDrawable("image_ui_dialog_asset_dialogborder");
        }
        dialogStyle.stageBackground = dimBackground;

        Dialog dialog = new Dialog("", dialogStyle);
        Table content = dialog.getContentTable();

        Table leftSide = new Table();
        Table rightSide = new Table();

        PamActor pamActor = new PamActor(pamPlayer, zombie.pamPath, "walk");
        pamActor.setPamScale(0.8f);
        leftSide.add(pamActor).size(200, 200);

        rightSide.add(new Label(zombie.name, skin, "big_outline")).left().padBottom(15).row();

        Table statsTable = new Table();

        TextureRegion toughIcon = textures.region("image_ui_almanac_zombies_zombietoughness_icon");
        if (toughIcon != null) statsTable.add(new Image(new TextureRegionDrawable(toughIcon))).size(40,40);
        statsTable.add(new Label("Toughness:\n" + zombie.hitpoints, skin, "medium")).padRight(30);

        TextureRegion speedIcon = textures.region("image_ui_almanac_zombies_zombiespeed_icon");
        if (speedIcon != null) statsTable.add(new Image(new TextureRegionDrawable(speedIcon))).size(40,40);
        statsTable.add(new Label("Speed:\n" + String.format("%.2f", zombie.speed), skin, "medium"));

        rightSide.add(statsTable).left().padBottom(10).row();
        rightSide.add(new Label("Attack Power (DPS): " + zombie.eatDPS, skin, "secondary")).left().row();

        content.add(leftSide).pad(20);
        content.add(rightSide).pad(20).top().left();

        dialog.getButtonTable().pad(20);
        TextButton closeBtn = new TextButton("Close", skin, "brown");
        dialog.button(closeBtn, true);

        dialog.show(getStage());
    }
}