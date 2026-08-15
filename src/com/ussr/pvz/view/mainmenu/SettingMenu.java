package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.controller.maincontroller.SettingController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.view.mainmenu.profile.ProfileUiFactory;
import pvz.libpvz.textures.TextureBank;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public final class SettingMenu extends Table {
    private static final String BACKGROUND_REGION =
            "IMAGE_MAINMENU_BACKGROUND";

    private static final String[] DIFFICULTY_NAMES = {
            "Easy",
            "Normal",
            "Medium",
            "Hard",
            "Very Hard"
    };

    private static final int DEFAULT_DIFFICULTY = 2;
    private static final float DEFAULT_GAME_SPEED = 1f;

    private enum Category {
        GAMEPLAY,
        TOOLS
    }

    private final Skin skin;
    private final SettingController controller;
    private final TextureBank textures;

    private Table contentRoot;
    private TextButton gameplayTab;
    private TextButton toolsTab;
    private Category selectedCategory;

    private int pendingDifficulty;
    private float pendingGameSpeed;
    private boolean pendingGridEnabled;
    private boolean pendingDebugEnabled;
    private boolean keyboardFocusInitialized;

    public SettingMenu(Skin skin) {
        this.skin = skin;
        this.controller = new SettingController();
        this.textures = new TextureBank(
                "768",
                Gdx.files.local("pvz-assets")
        );

        setFillParent(true);
        installKeyboardNavigation();
        readCurrentSettings();
        buildUi();
    }

    private void installKeyboardNavigation() {
        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode != Keys.ESCAPE) {
                    return false;
                }

                App.setMenuState(MenuState.MAIN);
                return true;
            }
        });
    }

    private void readCurrentSettings() {
        pendingDifficulty = currentDifficulty();
        pendingGameSpeed = controller.getGameSpeed();
        pendingGridEnabled = App.isGridEnabled();
        pendingDebugEnabled = App.isDebugModeEnabled();
    }

    private void buildUi() {
        Stack screen = new Stack();
        screen.add(createBackground());
        screen.add(createDimLayer());

        Table panelLayer = new Table();
        panelLayer.add(createPanel())
                .width(1120f)
                .height(650f);
        screen.add(panelLayer);

        add(screen).grow();
        showCategoryImmediately(Category.GAMEPLAY);
    }

    private Image createBackground() {
        TextureRegion region = textures.region(BACKGROUND_REGION);
        Image background = region == null
                ? new Image()
                : new Image(region);

        background.setScaling(Scaling.fill);
        background.setTouchable(Touchable.disabled);
        return background;
    }

    private Image createDimLayer() {
        Image dim = new Image(skin.newDrawable(
                "white-pixel",
                new Color(0.02f, 0.05f, 0.04f, 0.66f)
        ));
        dim.setTouchable(Touchable.disabled);
        return dim;
    }

    private Table createPanel() {
        Table panel = new Table();
        panel.setBackground(skin.getDrawable(
                "image_ui_dialog_asset_dialogborder_10"
        ));
        panel.pad(24f, 28f, 24f, 28f);

        panel.add(createHeader())
                .colspan(2)
                .growX()
                .height(66f)
                .padBottom(12f)
                .row();

        Table navigation = createNavigation();
        contentRoot = new Table();
        contentRoot.top();

        panel.add(navigation)
                .width(230f)
                .growY()
                .padRight(16f);

        panel.add(contentRoot)
                .grow()
                .row();

        panel.add(createFooter())
                .colspan(2)
                .growX()
                .height(66f)
                .padTop(14f);

        return panel;
    }

    private Table createHeader() {
        Table header = new Table();

        header.add().width(56f);
        header.add(new Label("SETTINGS", skin, "big_outline"))
                .expandX()
                .center();

        ImageButton closeButton = createCloseButton();
        header.add(closeButton).size(56f);
        return header;
    }

    private ImageButton createCloseButton() {
        ImageButton.ImageButtonStyle style =
                new ImageButton.ImageButtonStyle();

        style.imageUp = skin.getDrawable(
                "image_ui_generic_close_circle"
        );
        style.imageDown = skin.getDrawable(
                "image_ui_generic_close_circle_down"
        );

        ImageButton button = new ImageButton(style);
        button.addListener(ProfileUiFactory.listener(
                () -> App.setMenuState(MenuState.MAIN)
        ));
        return button;
    }

    private Table createNavigation() {
        Table navigation = new Table();
        navigation.top();
        navigation.pad(14f);
        navigation.setBackground(skin.newDrawable(
                "white-pixel",
                new Color(0.10f, 0.17f, 0.16f, 0.72f)
        ));

        Label categoryTitle = new Label(
                "CATEGORIES", skin, "medium_outline"
        );

        navigation.add(categoryTitle)
                .growX()
                .left()
                .pad(4f, 8f, 14f, 8f)
                .row();

        gameplayTab = createCategoryButton("Gameplay");
        toolsTab = createCategoryButton("Tools");

        navigation.add(gameplayTab)
                .growX()
                .height(62f)
                .padBottom(10f)
                .row();

        navigation.add(toolsTab)
                .growX()
                .height(62f)
                .row();

        navigation.add().growY();

        Label hint = createLightLabel(
                "Changes are saved\nwhen you press APPLY."
        );
        hint.setAlignment(Align.center);

        navigation.add(hint)
                .growX()
                .padBottom(6f)
                .row();

        attachCategoryListeners();

        return navigation;
    }

    private void attachCategoryListeners() {
        gameplayTab.addListener(ProfileUiFactory.listener(
                () -> switchCategory(Category.GAMEPLAY)
        ));
        toolsTab.addListener(ProfileUiFactory.listener(
                () -> switchCategory(Category.TOOLS)
        ));
    }

    private TextButton createCategoryButton(String text) {
        TextButton button = new TextButton(text, skin, "brown");
        button.getLabel().setAlignment(Align.left);
        button.padLeft(18f);
        return button;
    }

    private Table createFooter() {
        Table footer = new Table();

        TextButton resetButton = new TextButton(
                "Reset Defaults",
                skin,
                "brown"
        );

        TextButton applyButton = new TextButton(
                "APPLY",
                skin,
                "green"
        );

        resetButton.addListener(ProfileUiFactory.listener(
                this::resetDefaults
        ));
        applyButton.addListener(ProfileUiFactory.listener(
                this::applySettings
        ));

        footer.add(resetButton)
                .width(230f)
                .height(58f)
                .left();

        footer.add().growX();

        footer.add(createLightLabel("Esc / Back"))
                .padRight(18f);

        footer.add(applyButton)
                .width(260f)
                .height(62f)
                .right();

        return footer;
    }

    private void switchCategory(Category category) {
        if (category == selectedCategory) {
            return;
        }

        contentRoot.clearActions();
        contentRoot.addAction(sequence(
                fadeOut(0.10f, Interpolation.fade),
                run(() -> replaceCategoryContent(category)),
                fadeIn(0.16f, Interpolation.fade)
        ));
    }

    private void showCategoryImmediately(Category category) {
        replaceCategoryContent(category);
        contentRoot.getColor().a = 1f;
    }

    private void replaceCategoryContent(Category category) {
        selectedCategory = category;
        contentRoot.clearChildren();

        Actor content = category == Category.GAMEPLAY
                ? createGameplayContent()
                : createToolsContent();

        contentRoot.add(content).grow();
        updateCategoryStyles();
    }

    private void updateCategoryStyles() {
        boolean gameplaySelected =
                selectedCategory == Category.GAMEPLAY;

        gameplayTab.setStyle(skin.get(
                gameplaySelected ? "green" : "brown",
                TextButton.TextButtonStyle.class
        ));

        toolsTab.setStyle(skin.get(
                gameplaySelected ? "brown" : "green",
                TextButton.TextButtonStyle.class
        ));
    }

    private Table createGameplayContent() {
        Table content = createContentTable(
                "GAMEPLAY",
                "Tune the challenge and pace of each level."
        );

        content.add(createSettingCard(
                        "Difficulty",
                        "Controls zombie strength and overall challenge.",
                        createDifficultyControl()
                ))
                .growX()
                .height(128f)
                .padBottom(14f)
                .row();

        content.add(createSettingCard(
                        "Game Speed",
                        "Adjust how quickly gameplay actions progress.",
                        createSpeedControl()
                ))
                .growX()
                .height(150f)
                .row();

        content.add().growY();
        return content;
    }

    private Table createToolsContent() {
        Table content = createContentTable(
                "TOOLS",
                "Optional helpers for testing and board inspection."
        );

        content.add(createSettingCard(
                        "Show Lawn Grid",
                        "Display tile guides during gameplay.",
                        createGridToggle()
                ))
                .growX()
                .height(128f)
                .padBottom(14f)
                .row();

        content.add(createSettingCard(
                        "Debug Tools",
                        "Show testing buttons and resource controls.",
                        createDebugToggle()
                ))
                .growX()
                .height(128f)
                .row();

        content.add().growY();
        return content;
    }

    private Table createContentTable(
            String title,
            String description
    ) {
        Table content = new Table();
        content.top();

        Label titleLabel = new Label(
                title,
                skin,
                "medium_outline"
        );

        Label descriptionLabel = createLightLabel(description);

        content.add(titleLabel)
                .growX()
                .left()
                .pad(2f, 4f, 2f, 4f)
                .row();

        content.add(descriptionLabel)
                .growX()
                .left()
                .pad(0f, 4f, 14f, 4f)
                .row();

        return content;
    }

    private Table createSettingCard(
            String title,
            String description,
            Actor control
    ) {
        Table card = new Table();
        card.setBackground(skin.getDrawable(
                "image_ui_dialog_asset_inner_bkgd_10"
        ));
        card.pad(16f, 20f, 16f, 20f);

        Table copy = new Table();
        copy.left();

        copy.add(ProfileUiFactory.sectionTitle(skin, title))
                .growX()
                .left()
                .row();

        Label details = ProfileUiFactory.cardText(
                skin,
                description
        );
        details.setWrap(true);

        copy.add(details)
                .width(350f)
                .left()
                .padTop(5f)
                .row();

        card.add(copy).growX().left();
        card.add(control)
                .width(330f)
                .right();
        return card;
    }

    private Table createDifficultyControl() {
        Table control = new Table();

        TextButton previous = new TextButton("<", skin, "brown");
        TextButton next = new TextButton(">", skin, "brown");
        Label value = createControlValueLabel(
                difficultyName(pendingDifficulty)
        );

        previous.addListener(ProfileUiFactory.listener(() -> {
            pendingDifficulty = Math.max(1, pendingDifficulty - 1);
            value.setText(difficultyName(pendingDifficulty));
        }));

        next.addListener(ProfileUiFactory.listener(() -> {
            pendingDifficulty = Math.min(
                    DIFFICULTY_NAMES.length,
                    pendingDifficulty + 1
            );
            value.setText(difficultyName(pendingDifficulty));
        }));

        control.add(previous).size(54f);
        control.add(value).width(205f).height(54f).pad(0f, 8f, 0f, 8f);
        control.add(next).size(54f);
        return control;
    }

    private Table createSpeedControl() {
        Table control = new Table();
        Slider slider = new Slider(
                1f,
                3f,
                0.05f,
                false,
                skin,
                "default-horizontal"
        );

        slider.setValue(pendingGameSpeed);
        slider.setAnimateDuration(0.08f);
        slider.setVisualInterpolation(Interpolation.smooth);

        Label value = createControlValueLabel(
                formatSpeed(pendingGameSpeed)
        );

        Table markers = new Table();
        markers.add(ProfileUiFactory.cardText(skin, "1x")).left();
        markers.add(ProfileUiFactory.cardText(skin, "2x")).expandX();
        markers.add(ProfileUiFactory.cardText(skin, "3x")).right();

        control.add(value)
                .growX()
                .height(38f)
                .row();

        control.add(slider)
                .growX()
                .height(34f)
                .padTop(2f)
                .row();

        control.add(markers)
                .growX()
                .padTop(2f)
                .row();

        slider.addListener(ProfileUiFactory.listener(() -> {
            pendingGameSpeed = slider.getValue();
            value.setText(formatSpeed(pendingGameSpeed));
        }));

        return control;
    }

    private TextButton createGridToggle() {
        TextButton button = createToggleButton(pendingGridEnabled);
        button.addListener(ProfileUiFactory.listener(() -> {
            pendingGridEnabled = !pendingGridEnabled;
            updateToggleButton(button, pendingGridEnabled);
        }));
        return button;
    }

    private TextButton createDebugToggle() {
        TextButton button = createToggleButton(pendingDebugEnabled);
        button.addListener(ProfileUiFactory.listener(() -> {
            pendingDebugEnabled = !pendingDebugEnabled;
            updateToggleButton(button, pendingDebugEnabled);
        }));
        return button;
    }

    private TextButton createToggleButton(boolean enabled) {
        TextButton button = new TextButton(
                enabled ? "ON" : "OFF",
                skin,
                enabled ? "green" : "brown"
        );
        button.setSize(150f, 58f);
        return button;
    }

    private void updateToggleButton(
            TextButton button,
            boolean enabled
    ) {
        button.setText(enabled ? "ON" : "OFF");
        button.setStyle(skin.get(
                enabled ? "green" : "brown",
                TextButton.TextButtonStyle.class
        ));
    }

    private Label createControlValueLabel(String text) {
        Label label = ProfileUiFactory.sectionTitle(skin, text);
        label.setAlignment(Align.center);
        return label;
    }

    private Label createLightLabel(String text) {
        Label.LabelStyle style = new Label.LabelStyle(
                skin.get("default", Label.LabelStyle.class)
        );
        style.fontColor = Color.WHITE;
        return new Label(text, style);
    }

    private void resetDefaults() {
        pendingDifficulty = DEFAULT_DIFFICULTY;
        pendingGameSpeed = DEFAULT_GAME_SPEED;
        pendingGridEnabled = false;
        pendingDebugEnabled = false;

        replaceCategoryContent(selectedCategory);
        NotificationCenter.info(
                "Defaults selected. Press APPLY to save them."
        );
    }

    private void applySettings() {
        if (App.getAccount() == null) {
            NotificationCenter.error(
                    "You must be logged in to save settings."
            );
            return;
        }

        String difficultyResult = controller.changeDifficulty(
                pendingDifficulty
        );
        if (!wasSuccessful(difficultyResult)) {
            NotificationCenter.error(difficultyResult);
            return;
        }

        String speedResult = controller.changeGameSpeed(
                pendingGameSpeed
        );
        if (!wasSuccessful(speedResult)) {
            NotificationCenter.error(speedResult);
            return;
        }

        App.setGridEnabled(pendingGridEnabled);
        App.setDebugModeEnabled(pendingDebugEnabled);
        NotificationCenter.success("Settings applied successfully.");
    }

    private boolean wasSuccessful(String result) {
        return result != null && result.contains("successfully");
    }

    private int currentDifficulty() {
        if (App.getAccount() == null) {
            return DEFAULT_DIFFICULTY;
        }

        return Math.max(
                1,
                Math.min(
                        DIFFICULTY_NAMES.length,
                        App.getAccount().getDifficultyLvl()
                )
        );
    }

    private String difficultyName(int difficulty) {
        return DIFFICULTY_NAMES[difficulty - 1];
    }

    private String formatSpeed(float value) {
        return String.format("%.2fx", value);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!keyboardFocusInitialized && getStage() != null) {
            getStage().setKeyboardFocus(this);
            keyboardFocusInitialized = true;
        }
    }
}
