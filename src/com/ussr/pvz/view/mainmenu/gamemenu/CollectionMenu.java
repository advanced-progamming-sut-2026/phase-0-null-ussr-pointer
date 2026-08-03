package com.ussr.pvz.view.mainmenu.gamemenu;

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

        // FIX: Lowercase texture keys matching the atlas
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

        this.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainLayout.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.addActor(mainLayout);

        btnPlants.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { loadPlantsTab(); }
        });
        btnZombies.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { loadZombiesTab(); }
        });
// After building the layout, in buildUI():
    }

    private void loadPlantsTab() {
        contentTable.clearChildren();
        List<PlantData> plants = collectionService.getPlantDataForGUI();

        int columns = 6;
        int currentCount = 0;

        for (PlantData plant : plants) {
            Table card = new Table();
            card.setTouchable(Touchable.enabled);

            // FIX: Lowercase texture key
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
                card.setColor(Color.GRAY); // Changed to GRAY so the text remains readable
            }

            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    PlantCardOverlay overlay = new PlantCardOverlay(
                            plant, skin, textures, pamPlayer, dimBackground, collectionService
                    );
                    overlay.show(getStage());
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

            // FIX: Lowercase texture key
            TextureRegion cardBg = textures.region("image_ui_cards_almanac_zombie_card");
            if (cardBg != null) {
                card.setBackground(new TextureRegionDrawable(cardBg));
            }

            if (zombie.encountered) {
                PamActor pamActor = new PamActor(pamPlayer, zombie.pamPath, "walk");
                pamActor.setPamScale(0.3f);
                card.add(pamActor).size(80, 80).padTop(10).row();

                Label nameLbl = new Label(zombie.name, skin, "default");
                nameLbl.setFontScale(0.7f);
                nameLbl.setAlignment(Align.center);
                card.add(nameLbl).bottom().pad(5);

                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        showZombieDetailsOverlay(zombie);
                    }
                });
            } else {
                card.setColor(Color.BLACK);
                Label question = new Label("???", skin, "big");
                question.setAlignment(Align.center);
                card.add(question).expand().center();
            }

            contentTable.add(card).size(110, 160).pad(10);
            currentCount++;
            if (currentCount >= columns) {
                contentTable.row();
                currentCount = 0;
            }
        }
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

        // FIX: Lowercase texture keys
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