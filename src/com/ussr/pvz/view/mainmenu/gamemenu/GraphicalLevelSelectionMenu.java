package com.ussr.pvz.view.mainmenu.gamemenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.controller.maincontroller.LevelSelectionController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.notification.NotificationCenter;
import pvz.libpvz.textures.TextureBank;

import java.util.Comparator;
import java.util.List;

public class GraphicalLevelSelectionMenu extends Table {
    private static final String BACKGROUND_REGION = "IMAGE_MAINMENU_BACKGROUND";

    private final Skin skin;
    private final TextureBank textures;
    private final LevelSelectionController controller;

    public GraphicalLevelSelectionMenu(Skin skin) {
        this.skin = skin;
        this.controller = new LevelSelectionController();
        FileHandle assetsFolder = Gdx.files.local("pvz-assets");
        this.textures = new TextureBank("768", assetsFolder);
        setFillParent(true);
        buildUi();
    }

    private void buildUi() {
        Chapter chapter = App.getLevelManager().getCurrentChapter();
        if (chapter == null) {
            add(new Label("No chapter selected", skin, "big_outline"));
            return;
        }

        Stack layers = new Stack();
        layers.add(createBackground());
        layers.add(createDimLayer());
        layers.add(createContent(chapter));
        add(layers).grow();
    }

    private Image createBackground() {
        TextureRegion region = textures.region(BACKGROUND_REGION);
        Image background = region == null ? new Image() : new Image(region);
        background.setScaling(Scaling.fill);
        background.setTouchable(Touchable.disabled);
        return background;
    }

    private Image createDimLayer() {
        Image dim = new Image(skin.newDrawable(
                "white-pixel",
                new Color(0f, 0f, 0f, 0.32f)
        ));
        dim.setTouchable(Touchable.disabled);
        return dim;
    }

    private Table createContent(Chapter chapter) {
        Table content = new Table();
        content.center();

        Label title = new Label(chapter.getName(), skin, "big_outline");
        Label subtitle = new Label("Choose a level", skin, "medium_outline");
        ScrollPane levelScroll = createLevelScroll(chapter);

        content.add(title).padBottom(4f).row();
        content.add(subtitle).padBottom(18f).row();
        content.add(levelScroll).width(900f).height(340f);
        return content;
    }

    private ScrollPane createLevelScroll(Chapter chapter) {
        Table levelRow = new Table();
        List<Level> levels = chapter.getLevels().stream()
                .sorted(Comparator.comparingInt(Level::getOrder))
                .toList();

        for (Level level : levels) {
            levelRow.add(createLevelNode(level))
                    .width(205f)
                    .height(300f)
                    .padLeft(12f)
                    .padRight(12f);
        }

        ScrollPane scroll = new ScrollPane(levelRow);
        scroll.setScrollingDisabled(false, true);
        scroll.setOverscroll(false, false);
        scroll.setSmoothScrolling(true);
        scroll.setFlickScroll(true);
        scroll.setFadeScrollBars(true);
        return scroll;
    }

    private Table createLevelNode(Level level) {
        boolean unlocked = isUnlocked(level);
        Table node = new Table();
        ImageButton button = createNodeButton(level, unlocked);
        button.addListener(listener(() -> selectLevel(level)));

        String title = isZombossNode(level)
                ? "Zomboss"
                : "Level " + level.getOrder();
        Label label = new Label(title, skin, "medium_outline");
        label.setAlignment(Align.center);

        node.add(button).width(195f).height(235f).row();
        node.add(label).width(195f).height(38f).padTop(4f).row();

        if (!unlocked) {
            Label locked = new Label("LOCKED", skin);
            locked.setColor(Color.LIGHT_GRAY);
            node.add(locked).padTop(2f);
        }
        return node;
    }

    private ImageButton createNodeButton(Level level, boolean unlocked) {
        Drawable normal = findNodeDrawable(level.getMenuRegion());
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = unlocked
                ? normal
                : tint(normal, new Color(0.38f, 0.38f, 0.38f, 0.72f));
        style.imageDown = tint(normal, new Color(0.78f, 0.78f, 0.78f, 1f));
        ImageButton button = new ImageButton(style);
        button.getImage().setScaling(Scaling.fit);
        return button;
    }

    private Drawable findNodeDrawable(String regionName) {
        if (regionName != null && !regionName.isBlank()) {
            TextureRegion region = textures.region(regionName);
            if (region != null) return new TextureRegionDrawable(region);
        }
        return skin.getDrawable("image_ui_generic_brownbutton_10");
    }

    private Drawable tint(Drawable drawable, Color color) {
        if (drawable instanceof TextureRegionDrawable regionDrawable) {
            return regionDrawable.tint(color);
        }
        return skin.newDrawable(drawable, color);
    }

    private boolean isUnlocked(Level level) {
        Account account = App.getAccount();
        Chapter chapter = App.getLevelManager().getCurrentChapter();
        return account != null
                && account.getAdventureProgress() != null
                && account.getAdventureProgress().isLevelUnlocked(
                        chapter,
                        level,
                        App.getLevelManager().getChapters()
                );
    }

    private boolean isZombossNode(Level level) {
        return level.getMenuRegion() != null
                && level.getMenuRegion().contains("ZOMBOSS");
    }

    private void selectLevel(Level level) {
        MenuState previous = App.getMenuState();
        String result = controller.selectLevel(level.getId());
        if (App.getMenuState() != previous) {
            NotificationCenter.success(result);
        } else {
            NotificationCenter.error(result);
        }
    }

    private ChangeListener listener(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        };
    }
}
