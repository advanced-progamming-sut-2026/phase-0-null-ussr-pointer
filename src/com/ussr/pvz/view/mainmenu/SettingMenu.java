package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.controller.maincontroller.SettingController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.notification.NotificationCenter;
import pvz.libpvz.textures.TextureBank;

public final class SettingMenu extends Table {

    private static final String BG = "IMAGE_MAINMENU_BACKGROUND";

    private static final String HEADER = "IMAGE_UI_SETTINGS_HEADER";
    private static final String CLOSE = "IMAGE_UI_SETTINGS_CLOSE";

    private static final String ICON_AUDIO = "IMAGE_UI_SETTINGS_ICON_AUDIO";
    private static final String ICON_GAMEPLAY = "IMAGE_UI_SETTINGS_ICON_GAMEPLAY";
    private static final String ICON_DISPLAY = "IMAGE_UI_SETTINGS_ICON_DISPLAY";
    private static final String ICON_ACCESSIBILITY = "IMAGE_UI_SETTINGS_ICON_ACCESSIBILITY";

    private static final String TAB_DARK = "IMAGE_UI_SETTINGS_TAB_DARK";
    private static final String TAB_GREEN = "IMAGE_UI_SETTINGS_TAB_GREEN";

    private static final String ARROW_LEFT = "IMAGE_UI_SETTINGS_ARROW_LEFT";
    private static final String ARROW_RIGHT = "IMAGE_UI_SETTINGS_ARROW_RIGHT";
    private static final String VALUE_PANEL = "IMAGE_UI_SETTINGS_VALUE_PANEL";

    private static final String ROW_LARGE = "IMAGE_UI_SETTINGS_ROW_LARGE";
    private static final String CONTENT_PANEL = "IMAGE_UI_SETTINGS_CONTENT_PANEL";

    private static final String SLIDER_TRACK = "IMAGE_UI_SETTINGS_SLIDER_TRACK";
    private static final String SLIDER_FILL = "IMAGE_UI_SETTINGS_SLIDER_FILL";
    private static final String SLIDER_KNOB = "IMAGE_UI_SETTINGS_SLIDER_KNOB";

    private static final String TOGGLE_OFF = "IMAGE_UI_SETTINGS_TOGGLE_OFF";
    private static final String TOGGLE_ON = "IMAGE_UI_SETTINGS_TOGGLE_ON";

    private static final String APPLY = "IMAGE_UI_SETTINGS_BUTTON_APPLY";
    private static final String RESET = "IMAGE_UI_SETTINGS_BUTTON_RESET";

    private static final String[] DIFFICULTY_NAMES = {
            "Easy", "Normal", "Medium", "Hard", "Very Hard"
    };

    private enum Category {
        AUDIO, GAMEPLAY, DISPLAY, ACCESSIBILITY
    }

    private final Skin skin;
    private final SettingController controller;
    private final TextureBank textures;

    private final Button[] tabs = new Button[4];
    private Table content;
    private Category selected = Category.GAMEPLAY;

    private int difficulty;
    private float gameSpeed;
    private boolean grid;
    private boolean debug;
    private boolean keyboardFocusInitialized;

    public SettingMenu(Skin skin) {
        this.skin = skin;
        this.controller = new SettingController();
        this.textures = new TextureBank("768", Gdx.files.local("pvz-assets"));

        difficulty = currentDifficulty();
        gameSpeed = controller.getGameSpeed();
        grid = App.isGridEnabled();
        debug = App.isDebugModeEnabled();

        setFillParent(true);
        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Keys.ESCAPE) {
                    App.setMenuState(MenuState.MAIN);
                    return true;
                }
                return false;
            }
        });

        build();
    }

    private void build() {
        Stack root = new Stack();
        root.add(background());

        Image dim = new Image(skin.newDrawable(
                "white-pixel",
                new Color(0f, 0f, 0f, 0.52f)
        ));
        dim.setTouchable(Touchable.disabled);
        root.add(dim);

        Table center = new Table();
        center.add(panel()).width(1080f).height(650f);
        root.add(center);

        add(root).grow();
        show(Category.GAMEPLAY);
    }

    private Actor panel() {
        Table panel = new Table();
        panel.setBackground(panelDrawable(CONTENT_PANEL, 28, 28, 28, 28));
        panel.pad(14f, 18f, 14f, 18f);

        panel.add(header()).colspan(2).growX().height(100f).row();

        Table nav = navigation();
        content = new Table();
        content.top();

        panel.add(nav).width(235f).growY().padRight(18f);
        panel.add(content).grow().row();

        panel.add(footer()).colspan(2).growX().height(85f);
        return panel;
    }

    private Actor header() {
        Table t = new Table();
        t.add().width(72f);

        Image title = image(HEADER);
        t.add(title).width(350f).height(110f).expandX().center();

        ImageButton close = imageButton(CLOSE);
        close.addListener(click(() -> App.setMenuState(MenuState.MAIN)));
        t.add(close).size(72f).right();
        return t;
    }

    private Table navigation() {
        Table nav = new Table();
        nav.top().padTop(8f);

        tabs[0] = tab("Audio", ICON_AUDIO, Category.AUDIO);
        tabs[1] = tab("Gameplay", ICON_GAMEPLAY, Category.GAMEPLAY);
        tabs[2] = tab("Display", ICON_DISPLAY, Category.DISPLAY);
        tabs[3] = tab("Accessibility", ICON_ACCESSIBILITY, Category.ACCESSIBILITY);

        for (Button tab : tabs) {
            nav.add(tab).width(210f).height(74f).padBottom(8f).row();
        }
        nav.add().growY();
        return nav;
    }

    private Button tab(String text, String iconRegion, Category category) {
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = panelDrawable(TAB_DARK, 24, 24, 24, 24);
        style.down = panelDrawable(TAB_GREEN, 24, 24, 24, 24);
        style.checked = panelDrawable(TAB_GREEN, 24, 24, 24, 24);

        Button b = new Button(style);
        Image icon = image(iconRegion);
        Label label = new Label(text, skin, "medium_outline");
        label.setAlignment(Align.left);

        b.add(icon).size(48f).padLeft(12f).padRight(8f);
        b.add(label).growX().left().padRight(10f);

        b.addListener(click(() -> show(category)));
        return b;
    }

    private Actor footer() {
        Table f = new Table();

        ImageButton reset = imageButton(RESET);
        reset.addListener(click(this::resetDefaults));

        ImageButton apply = imageButton(APPLY);
        apply.addListener(click(this::applySettings));

        f.add(reset).width(230f).height(58f).left();
        f.add().growX();

        Label esc = new Label("Esc / Back", skin, "default");
        esc.setColor(new Color(0.9f, 0.86f, 0.70f, 1f));
        f.add(esc).padRight(18f);

        f.add(apply).width(250f).height(82f).right();
        return f;
    }

    private void show(Category category) {
        selected = category;
        if (content == null) return;

        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setChecked(i == category.ordinal());
        }

        content.clearChildren();

        switch (category) {
            case AUDIO:
                content.add(audioContent()).grow();
                break;
            case GAMEPLAY:
                content.add(gameplayContent()).grow();
                break;
            case DISPLAY:
                content.add(displayContent()).grow();
                break;
            case ACCESSIBILITY:
                content.add(accessibilityContent()).grow();
                break;
        }
    }

    private Actor audioContent() {
        Table box = section();
        box.add(title("Audio")).growX().left().padBottom(12f).row();

        Table card = rowCard();
        Label heading = title("Audio");
        Label copy = description(
                "Audio controls can be wired to your game's audio manager here."
        );
        card.add(copyBlock(heading, copy)).growX().left();

        box.add(card).growX().height(125f).row();
        box.add().growY();
        return box;
    }

    private Actor gameplayContent() {
        Table box = section();

        box.add(difficultyRow()).growX().height(126f).padBottom(14f).row();
        box.add(speedRow()).growX().height(126f).row();
        box.add().growY();

        return box;
    }

    private Actor displayContent() {
        Table box = section();
        box.add(toggleRow(
                "Show Lawn Grid",
                "Display tile guides during gameplay.",
                () -> grid,
                value -> grid = value
        )).growX().height(126f).row();
        box.add().growY();
        return box;
    }

    private Actor accessibilityContent() {
        Table box = section();
        box.add(toggleRow(
                "Debug Tools",
                "Show testing and resource controls.",
                () -> debug,
                value -> debug = value
        )).growX().height(126f).row();
        box.add().growY();
        return box;
    }

    private Table section() {
        Table t = new Table();
        t.top();
        t.pad(8f, 2f, 2f, 2f);
        return t;
    }

    private Table difficultyRow() {
        Table row = rowCard();

        Table copy = copyBlock(
                title("Difficulty"),
                description("Controls zombie strength")
        );

        ImageButton left = imageButton(ARROW_LEFT);
        ImageButton right = imageButton(ARROW_RIGHT);

        Label value = new Label(
                difficultyName(difficulty),
                skin,
                "medium_outline"
        );
        value.setAlignment(Align.center);
        value.setColor(new Color(0.28f, 0.14f, 0.06f, 1f));

        Stack valueBox = new Stack();
        Image valueBg = image(VALUE_PANEL);
        valueBg.setScaling(Scaling.stretch);
        valueBg.setTouchable(Touchable.disabled);
        valueBox.add(valueBg);
        valueBox.add(value);

        left.addListener(click(() -> {
            difficulty = Math.max(1, difficulty - 1);
            value.setText(difficultyName(difficulty));
        }));

        right.addListener(click(() -> {
            difficulty = Math.min(DIFFICULTY_NAMES.length, difficulty + 1);
            value.setText(difficultyName(difficulty));
        }));

        Table controls = new Table();
        controls.add(left).size(54f);
        controls.add(valueBox).width(150f).height(54f).pad(0, 8f, 0, 8f);
        controls.add(right).size(54f);

        row.add(copy).growX().left();
        row.add(controls).width(300f).right().padRight(12f);
        return row;
    }

    private Table speedRow() {
        Table row = rowCard();

        Table copy = copyBlock(
                title("Game Speed"),
                description("Adjust gameplay speed")
        );

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = drawable(SLIDER_TRACK);
        sliderStyle.knob = drawable(SLIDER_KNOB);
        sliderStyle.knobBefore = drawable(SLIDER_FILL);

        Slider slider = new Slider(1f, 3f, 0.05f, false, sliderStyle);
        slider.setValue(gameSpeed);
        slider.setAnimateDuration(0.08f);
        slider.setVisualInterpolation(Interpolation.smooth);

        Label value = new Label(
                String.format("%.2fx", gameSpeed),
                skin,
                "medium_outline"
        );
        value.setAlignment(Align.center);
        value.setColor(new Color(0.28f, 0.14f, 0.06f, 1f));

        slider.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button) {
                update();
                return false;
            }

            @Override
            public void touchDragged(InputEvent e, float x, float y, int pointer) {
                update();
            }

            @Override
            public void touchUp(InputEvent e, float x, float y, int pointer, int button) {
                update();
            }

            private void update() {
                gameSpeed = slider.getValue();
                value.setText(String.format("%.2fx", gameSpeed));
            }
        });

        Table controls = new Table();
        controls.add(value).width(90f).padRight(8f);
        controls.add(slider).width(260f).height(46f);

        row.add(copy).growX().left();
        row.add(controls).width(370f).right().padRight(12f);
        return row;
    }

    private interface BoolGetter { boolean get(); }
    private interface BoolSetter { void set(boolean value); }

    private Table toggleRow(
            String headingText,
            String descriptionText,
            BoolGetter getter,
            BoolSetter setter
    ) {
        Table row = rowCard();

        row.add(copyBlock(
                title(headingText),
                description(descriptionText)
        )).growX().left();

        ImageButton toggle = imageButton(getter.get() ? TOGGLE_ON : TOGGLE_OFF);
        toggle.addListener(click(() -> {
            boolean next = !getter.get();
            setter.set(next);
            setButtonRegion(toggle, next ? TOGGLE_ON : TOGGLE_OFF);
        }));

        row.add(toggle).width(130f).height(60f).right().padRight(14f);
        return row;
    }

    private Table rowCard() {
        Table row = new Table();
        row.setBackground(panelDrawable(ROW_LARGE, 24, 24, 24, 24));
        row.pad(12f, 20f, 12f, 20f);
        return row;
    }

    private Table copyBlock(Label heading, Label details) {
        Table copy = new Table();
        copy.left();
        copy.add(heading).growX().left().row();
        copy.add(details).growX().left().padTop(3f).row();
        return copy;
    }

    private Label title(String text) {
        Label l = new Label(text, skin, "medium_outline");
        l.setColor(new Color(0.27f, 0.13f, 0.05f, 1f));
        return l;
    }

    private Label description(String text) {
        Label l = new Label(text, skin, "default");
        l.setColor(new Color(0.35f, 0.26f, 0.17f, 1f));
        l.setWrap(true);
        return l;
    }

    private Image background() {
        TextureRegion region = textures.region(BG);
        Image i = region == null ? new Image() : new Image(region);
        i.setScaling(Scaling.fill);
        i.setTouchable(Touchable.disabled);
        return i;
    }

    private Image image(String name) {
        TextureRegion region = required(name);
        Image i = new Image(region);
        i.setScaling(Scaling.fit);
        return i;
    }

    private TextureRegionDrawable drawable(String name) {
        return new TextureRegionDrawable(required(name));
    }

    private NinePatchDrawable panelDrawable(
            String name,
            int left,
            int right,
            int top,
            int bottom
    ) {
        return new NinePatchDrawable(
                new NinePatch(required(name), left, right, top, bottom)
        );
    }

    private ImageButton imageButton(String name) {
        TextureRegionDrawable up = drawable(name);
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = up;
        style.imageDown = up.tint(new Color(0.82f, 0.82f, 0.82f, 1f));

        ImageButton b = new ImageButton(style);
        b.getImage().setScaling(Scaling.fit);
        return b;
    }

    private void setButtonRegion(ImageButton b, String name) {
        TextureRegionDrawable up = drawable(name);
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = up;
        style.imageDown = up.tint(new Color(0.82f, 0.82f, 0.82f, 1f));
        b.setStyle(style);
        b.getImage().setScaling(Scaling.fit);
    }

    private TextureRegion required(String name) {
        TextureRegion region = textures.region(name);
        if (region == null) {
            throw new IllegalStateException("Missing settings texture: " + name);
        }
        return region;
    }

    private InputListener click(Runnable runnable) {
        return new InputListener() {
            private boolean pressed;

            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button) {
                pressed = true;
                return true;
            }

            @Override
            public void touchUp(InputEvent e, float x, float y, int pointer, int button) {
                if (!pressed) return;
                pressed = false;

                Actor a = e.getListenerActor();
                if (x >= 0 && y >= 0 && x <= a.getWidth() && y <= a.getHeight()) {
                    runnable.run();
                }
            }
        };
    }

    private void resetDefaults() {
        difficulty = 2;
        gameSpeed = 1f;
        grid = false;
        debug = false;
        show(selected);
        NotificationCenter.info("Defaults selected. Press APPLY to save them.");
    }

    private void applySettings() {
        if (App.getAccount() == null) {
            NotificationCenter.error("You must be logged in to save settings.");
            return;
        }

        String difficultyResult = controller.changeDifficulty(difficulty);
        if (!ok(difficultyResult)) {
            NotificationCenter.error(difficultyResult);
            return;
        }

        String speedResult = controller.changeGameSpeed(gameSpeed);
        if (!ok(speedResult)) {
            NotificationCenter.error(speedResult);
            return;
        }

        App.setGridEnabled(grid);
        App.setDebugModeEnabled(debug);
        NotificationCenter.success("Settings applied successfully.");
    }

    private boolean ok(String result) {
        return result != null && result.contains("successfully");
    }

    private int currentDifficulty() {
        if (App.getAccount() == null) return 2;
        return Math.max(
                1,
                Math.min(
                        DIFFICULTY_NAMES.length,
                        App.getAccount().getDifficultyLvl()
                )
        );
    }

    private String difficultyName(int value) {
        return DIFFICULTY_NAMES[value - 1];
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
