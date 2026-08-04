package com.ussr.pvz.view.mainmenu.gamemenu.collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.ussr.pvz.view.animation.ZombiePamActor;
import com.ussr.pvz.view.components.PlantCard;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.List;
import java.util.stream.Collectors;

public class CollectionMenu extends FadingMenu {
    private final Skin skin;
    private final CollectionService collectionService;
    private final TextureBank textures;
    private final PamPlayer pamPlayer;

    private Table contentTable;
    private Table filterBar;
    private ScrollPane scrollPane;
    private Table mainLayout;
    private TextureRegionDrawable dimBackground;
    private SelectBox<String> categoryBox;

    // Active filter buttons (fields so setActive lambda can reach them)
    private TextButton btnAll, btnUnlocked, btnLocked, btnUpgradeable;

    private String activeCategoryFilter = "ALL";
    private String activeLockFilter     = "ALL";
    private boolean upgradeableOnly     = false;
    private List<PlantData> allPlants;

    // ── Init ──────────────────────────────────────────────────────────────────

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

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void buildUI() {
        mainLayout = new Table();
        mainLayout.setFillParent(true);
        applyBackground();

        contentTable = new Table();
        contentTable.top().pad(20);
        scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        mainLayout.add(buildTabsTable()).top().expandX().fillX().padTop(10).row();
        mainLayout.add(buildFilterBar()).expandX().fillX().row();
        mainLayout.add(scrollPane).expand().fill().pad(20);
        this.addActor(mainLayout);

        loadPlantsTab();
    }

    private void applyBackground() {
        TextureRegion bg = textures.region("image_ui_quests_travel_log_final");
        if (bg == null) bg = textures.region("image_ui_quests_travel_log_corner");
        if (bg != null) mainLayout.setBackground(new TextureRegionDrawable(bg));
    }

    private Table buildTabsTable() {
        Table tabs = new Table();
        TextButton btnPlants  = new TextButton("Plants",  skin, "green");
        TextButton btnZombies = new TextButton("Zombies", skin, "purple");
        tabs.add(btnPlants).pad(10).width(200);
        tabs.add(btnZombies).pad(10).width(200);

        btnPlants.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                filterBar.setVisible(true);
                loadPlantsTab();
            }
        });
        btnZombies.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                filterBar.setVisible(false);
                loadZombiesTab();
            }
        });
        return tabs;
    }

    // ── Filter Bar ────────────────────────────────────────────────────────────

    private Table buildFilterBar() {
        filterBar = new Table();
        filterBar.pad(6, 16, 6, 16);
        filterBar.defaults().padRight(8);

        addLockFilters(filterBar);
        filterBar.add(new Label("|", skin, "default")).padLeft(4).padRight(4);
        addUpgradeableFilter(filterBar);
        filterBar.add(new Label("|", skin, "default")).padLeft(4).padRight(4);
        addCategoryFilter(filterBar);

        return filterBar;
    }

    private void addLockFilters(Table bar) {
        bar.add(new Label("Show:", skin, "default"));

        btnAll      = new TextButton("All",      skin, "default");
        btnUnlocked = new TextButton("Unlocked", skin, "default");
        btnLocked   = new TextButton("Locked",   skin, "default");
        setLockActive(btnAll);

        btnAll.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                activeLockFilter = "ALL"; setLockActive(btnAll); refreshPlantsTab();
            }
        });
        btnUnlocked.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                activeLockFilter = "UNLOCKED"; setLockActive(btnUnlocked); refreshPlantsTab();
            }
        });
        btnLocked.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                activeLockFilter = "LOCKED"; setLockActive(btnLocked); refreshPlantsTab();
            }
        });

        bar.add(btnAll).width(80);
        bar.add(btnUnlocked).width(90);
        bar.add(btnLocked).width(80);
    }

    private void setLockActive(TextButton active) {
        btnAll.setColor(Color.GRAY);
        btnUnlocked.setColor(Color.GRAY);
        btnLocked.setColor(Color.GRAY);
        active.setColor(Color.WHITE);
    }

    private void addUpgradeableFilter(Table bar) {
        btnUpgradeable = new TextButton("Upgradeable", skin, "default");
        btnUpgradeable.setColor(Color.GRAY);
        btnUpgradeable.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                upgradeableOnly = !upgradeableOnly;
                btnUpgradeable.setColor(upgradeableOnly ? Color.WHITE : Color.GRAY);
                refreshPlantsTab();
            }
        });
        bar.add(btnUpgradeable).width(120);
    }

    private void addCategoryFilter(Table bar) {
        bar.add(new Label("Family:", skin, "default")).padLeft(4);
        categoryBox = new SelectBox<>(skin);
        categoryBox.setItems("ALL");
        categoryBox.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                activeCategoryFilter = categoryBox.getSelected();
                refreshPlantsTab();
            }
        });
        bar.add(categoryBox).width(140);
    }

    // ── Plants ────────────────────────────────────────────────────────────────

    private void loadPlantsTab() {
        allPlants = collectionService.getPlantDataForGUI();
        populateCategoryBox();
        refreshPlantsTab();
    }

    private void populateCategoryBox() {
        List<String> categories = allPlants.stream()
                .map(p -> p.category)
                .filter(c -> c != null && !c.isBlank())
                .distinct().sorted()
                .collect(Collectors.toList());
        categories.add(0, "ALL");
        categoryBox.setItems(categories.toArray(new String[0]));
        categoryBox.setSelected("ALL");
    }

    private void refreshPlantsTab() {
        contentTable.clearChildren();
        if (allPlants == null) return;

        List<PlantData> filtered = allPlants.stream()
                .filter(this::passesLockFilter)
                .filter(this::passesUpgradeFilter)
                .filter(this::passesCategoryFilter)
                .collect(Collectors.toList());

        renderPlantCards(filtered);
    }

    private boolean passesLockFilter(PlantData p) {
        if ("UNLOCKED".equals(activeLockFilter)) return p.level > 0;
        if ("LOCKED".equals(activeLockFilter))   return p.level == 0;
        return true;
    }

    private boolean passesUpgradeFilter(PlantData p) {
        if (!upgradeableOnly) return true;
        return p.level > 0 && p.level < 4 && p.ownedPackets > 0;
    }

    private boolean passesCategoryFilter(PlantData p) {
        if ("ALL".equals(activeCategoryFilter)) return true;
        return activeCategoryFilter.equals(p.category);
    }

    private void renderPlantCards(List<PlantData> plants) {
        int columns = 6, col = 0;
        for (PlantData plant : plants) {
            PlantCard card = new PlantCard(plant, skin, textures, () -> showPlantDetailsOverlay(plant));
            contentTable.add(card).size(110, 160).pad(10);
            if (++col >= columns) { contentTable.row(); col = 0; }
        }
        if (plants.isEmpty()) {
            Label empty = new Label("No plants match the filter.", skin, "default");
            empty.setAlignment(Align.center);
            contentTable.add(empty).colspan(6).pad(40);
        }
    }

    // ── Zombies ───────────────────────────────────────────────────────────────

    private void loadZombiesTab() {
        contentTable.clearChildren();
        List<ZombieData> zombies = collectionService.getZombieDataForGUI();
        int columns = 6, col = 0;
        for (ZombieData zombie : zombies) {
            contentTable.add(buildZombieCard(zombie)).size(110, 160).pad(10);
            if (++col >= columns) { contentTable.row(); col = 0; }
        }
    }

    private Table buildZombieCard(ZombieData zombie) {
        Table card = new Table();
        card.setTouchable(Touchable.enabled);

        TextureRegion cardBg = textures.region("image_ui_cards_almanac_zombie_card");
        if (cardBg != null) card.setBackground(new TextureRegionDrawable(cardBg));

        Stack animStack = new Stack();
        TextureRegion floor = textures.region("image_ui_dialog_asset_dialogtexture");
        if (floor != null) animStack.add(new Image(new TextureRegionDrawable(floor)));
        ZombiePamActor pam = new ZombiePamActor(pamPlayer, zombie.pamPath);
        pam.setPamScale(0.3f); pam.setOffsetY(-15f);
        animStack.add(pam);
        card.add(animStack).size(80, 80).padTop(10).row();

        Label name = new Label(zombie.name, skin, "default");
        name.setFontScale(0.65f); name.setAlignment(Align.center);
        card.add(name).bottom().pad(5);
        card.setColor(zombie.encountered ? Color.WHITE : Color.DARK_GRAY);

        card.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                showZombieDetailsOverlay(zombie);
            }
        });
        return card;
    }

    // ── Overlays ──────────────────────────────────────────────────────────────

    private void showPlantDetailsOverlay(PlantData plant) {
        new PlantCardOverlay(plant, skin, textures, pamPlayer, dimBackground, collectionService)
                .show(getStage());
    }

    private void showZombieDetailsOverlay(ZombieData zombie) {
        new ZombieCardOverlay(zombie, skin, textures, pamPlayer, dimBackground)
                .show(getStage());
    }
}