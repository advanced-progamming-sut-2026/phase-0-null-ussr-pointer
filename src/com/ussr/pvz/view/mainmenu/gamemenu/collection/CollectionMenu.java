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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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
    private static final Color FILTER_ACTIVE_COLOR =
            new Color(1f, 0.88f, 0.48f, 1f);
    private static final Color FILTER_INACTIVE_COLOR =
            new Color(0.52f, 0.52f, 0.52f, 1f);
    private static final Color FILTER_ACTIVE_TEXT =
            new Color(0.20f, 0.13f, 0.06f, 1f);
    private static final Color FILTER_INACTIVE_TEXT =
            new Color(0.82f, 0.82f, 0.82f, 1f);

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
    private Label resultsLabel;
    private TextButton plantsTabButton;
    private TextButton zombiesTabButton;

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

        mainLayout.add(buildTabsTable())
                .top().expandX().fillX()
                .pad(14, 24, 0, 24)
                .row();
        mainLayout.add(buildFilterBar())
                .expandX().fillX()
                .pad(8, 24, 0, 24)
                .row();
        mainLayout.add(scrollPane).expand().fill().pad(12, 20, 20, 20);
        this.addActor(mainLayout);

        loadPlantsTab();
    }

    private void applyBackground() {
        TextureRegion bg = textures.region("image_ui_quests_travel_log_final");
        if (bg == null) bg = textures.region("image_ui_quests_travel_log_corner");
        if (bg != null) mainLayout.setBackground(new TextureRegionDrawable(bg));
    }

    private Table buildTabsTable() {
        Table header = new Table();
        header.pad(12, 18, 10, 18);
        if (skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
            header.setBackground(skin.getDrawable(
                    "image_ui_dialog_asset_inner_bkgd_10"
            ));
        }

        Label title = new Label("COLLECTION", skin, "big_outline");
        Label subtitle = new Label(
                "Browse your plants and discovered zombies",
                skin,
                "default"
        );
        subtitle.setColor(new Color(0.25f, 0.20f, 0.14f, 1f));

        Table heading = new Table();
        heading.left();
        heading.add(title).left().row();
        heading.add(subtitle).left().padTop(2f);

        resultsLabel = new Label("", skin, "default");
        resultsLabel.setColor(new Color(0.25f, 0.20f, 0.14f, 1f));
        resultsLabel.setAlignment(Align.right);

        header.add(heading).growX().left();
        header.add(resultsLabel).width(250f).right().row();

        plantsTabButton = new TextButton("PLANTS", skin, "green");
        zombiesTabButton = new TextButton("ZOMBIES", skin, "purple");
        plantsTabButton.getLabel().setFontScale(0.9f);
        zombiesTabButton.getLabel().setFontScale(0.9f);

        Table tabs = new Table();
        tabs.left();
        tabs.add(plantsTabButton).width(220f).height(52f).padRight(8f);
        tabs.add(zombiesTabButton).width(220f).height(52f);
        header.add(tabs).colspan(2).growX().left().padTop(12f);

        plantsTabButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                filterBar.setVisible(true);
                setActiveTab(true);
                loadPlantsTab();
            }
        });
        zombiesTabButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                filterBar.setVisible(false);
                setActiveTab(false);
                loadZombiesTab();
            }
        });

        setActiveTab(true);
        return header;
    }

    private void setActiveTab(boolean plantsActive) {
        plantsTabButton.setColor(
                plantsActive ? Color.WHITE : new Color(0.52f, 0.52f, 0.52f, 1f)
        );
        zombiesTabButton.setColor(
                plantsActive ? new Color(0.52f, 0.52f, 0.52f, 1f) : Color.WHITE
        );
    }

    // ── Filter Bar ────────────────────────────────────────────────────────────

    private Table buildFilterBar() {
        filterBar = new Table();
        filterBar.pad(10, 14, 10, 14);
        filterBar.defaults().padRight(8f);
        if (skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
            filterBar.setBackground(skin.getDrawable(
                    "image_ui_dialog_asset_inner_bkgd_10"
            ));
        }

        addLockFilters(filterBar);
        filterBar.add().width(16f);
        addUpgradeableFilter(filterBar);
        filterBar.add().width(16f);
        addCategoryFilter(filterBar);
        filterBar.add().expandX();

        TextButton resetButton = new TextButton("RESET", skin, "brown");
        resetButton.getLabel().setFontScale(0.75f);
        resetButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                resetPlantFilters();
            }
        });
        filterBar.add(resetButton).width(105f).height(40f).padRight(0f);

        return filterBar;
    }

    private void addLockFilters(Table bar) {
        Label showLabel = new Label("Ownership", skin, "default");
        showLabel.setColor(new Color(0.25f, 0.20f, 0.14f, 1f));
        bar.add(showLabel).padRight(10f);

        btnAll      = new TextButton("All",      skin, "default");
        btnUnlocked = new TextButton("Unlocked", skin, "default");
        btnLocked   = new TextButton("Locked",   skin, "default");
        setLockActive(btnAll);

        btnAll.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectOwnershipFilter("ALL", btnAll);
            }
        });

        btnUnlocked.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectOwnershipFilter("UNLOCKED", btnUnlocked);
            }
        });

        btnLocked.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectOwnershipFilter("LOCKED", btnLocked);
            }
        });

        bar.add(btnAll).width(80);
        bar.add(btnUnlocked).width(90);
        bar.add(btnLocked).width(80);
    }

    private void selectOwnershipFilter(
            String filter,
            TextButton activeButton
    ) {
        activeLockFilter = filter;

        // Disable the upgradeable-only filter.
        upgradeableOnly = false;
        setFilterButtonState(btnUpgradeable, false);

        setLockActive(activeButton);
        refreshPlantsTab();
    }

    private void setLockActive(TextButton active) {
        setFilterButtonState(btnAll, btnAll == active);
        setFilterButtonState(btnUnlocked, btnUnlocked == active);
        setFilterButtonState(btnLocked, btnLocked == active);
    }

    private void setFilterButtonState(
            TextButton button,
            boolean active
    ) {
        button.setColor(
                active ? FILTER_ACTIVE_COLOR : FILTER_INACTIVE_COLOR
        );
        button.getLabel().setColor(
                active ? FILTER_ACTIVE_TEXT : FILTER_INACTIVE_TEXT
        );
        button.getLabel().setFontScale(active ? 0.82f : 0.72f);
    }

    private void addUpgradeableFilter(Table bar) {
        btnUpgradeable = new TextButton("Ready to upgrade", skin, "default");
        setFilterButtonState(btnUpgradeable, false);
        btnUpgradeable.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectUpgradeableFilter();
            }
        });
        bar.add(btnUpgradeable).width(145);
    }

    private void selectUpgradeableFilter() {
        upgradeableOnly = true;
        activeLockFilter = "ALL";

        setFilterButtonState(btnAll, false);
        setFilterButtonState(btnUnlocked, false);
        setFilterButtonState(btnLocked, false);
        setFilterButtonState(btnUpgradeable, true);

        refreshPlantsTab();
    }

    private void addCategoryFilter(Table bar) {
        Label familyLabel = new Label("Plant family", skin, "default");
        familyLabel.setColor(new Color(0.25f, 0.20f, 0.14f, 1f));
        bar.add(familyLabel).padLeft(4).padRight(8f);
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

    private void resetPlantFilters() {
        activeLockFilter = "ALL";
        activeCategoryFilter = "ALL";
        upgradeableOnly = false;

        setLockActive(btnAll);
        setFilterButtonState(btnUpgradeable, false);
        categoryBox.setSelected("ALL");
        refreshPlantsTab();
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
        if (categories.contains(activeCategoryFilter)) {
            categoryBox.setSelected(activeCategoryFilter);
        } else {
            activeCategoryFilter = "ALL";
            categoryBox.setSelected("ALL");
        }
    }

    private void refreshPlantsTab() {
        contentTable.clearChildren();
        if (allPlants == null) return;

        List<PlantData> filtered = allPlants.stream()
                .filter(this::passesLockFilter)
                .filter(this::passesUpgradeFilter)
                .filter(this::passesCategoryFilter)
                .collect(Collectors.toList());

        resultsLabel.setText(
                filtered.size() + " of " + allPlants.size() + " plants"
        );
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
        int columns = 4, col = 0;
        for (PlantData plant : plants) {
            PlantCard card = new PlantCard(plant, skin, textures, () -> showPlantDetailsOverlay(plant));
            contentTable.add(card).size(240, 135).pad(8);
            if (++col >= columns) { contentTable.row(); col = 0; }
        }
        if (plants.isEmpty()) {
            Label empty = new Label("No plants match the filter.", skin, "default");
            empty.setAlignment(Align.center);
            contentTable.add(empty).colspan(columns).pad(40);
        }
    }

    // ── Zombies ───────────────────────────────────────────────────────────────

    private void loadZombiesTab() {
        contentTable.clearChildren();
        List<ZombieData> zombies = collectionService.getZombieDataForGUI();
        long discovered = zombies.stream().filter(z -> z.encountered).count();
        resultsLabel.setText(
                discovered + " of " + zombies.size() + " discovered"
        );
        int columns = 4, col = 0;
        for (ZombieData zombie : zombies) {
            ZombieCard card = new ZombieCard(
                    zombie,
                    skin,
                    textures,
                    pamPlayer,
                    () -> showZombieDetailsOverlay(zombie)
            );
            contentTable.add(card).size(240, 135).pad(8);
            if (++col >= columns) { contentTable.row(); col = 0; }
        }

        if (zombies.isEmpty()) {
            Label empty = new Label(
                    "No zombies are available.",
                    skin,
                    "default"
            );
            empty.setAlignment(Align.center);
            contentTable.add(empty).colspan(columns).pad(40);
        }
    }

    // ── Overlays ──────────────────────────────────────────────────────────────

    private void showPlantDetailsOverlay(PlantData plant) {
        new PlantCardOverlay(
                plant,
                skin,
                textures,
                pamPlayer,
                dimBackground,
                collectionService,
                this::loadPlantsTab
        ).show(getStage());
    }

    private void showZombieDetailsOverlay(ZombieData zombie) {
        new ZombieCardOverlay(zombie, skin, textures, pamPlayer, dimBackground)
                .show(getStage());
    }
}