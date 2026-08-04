package com.ussr.pvz.view.mainmenu.gamemenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.model.level.GameMode;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameController;
import com.ussr.pvz.notification.NotificationCenter;
import pvz.libpvz.textures.TextureBank;

import java.util.List;


public class GameMenu extends Table {
    private final Skin skin;

    private final GameController controller;
    private final TextureBank textures;

    public GameMenu(Skin skin) {
        this.skin = skin;
        this.controller = new GameController();

        FileHandle assetsFolder = Gdx.files.local("pvz-assets");
        this.textures = new TextureBank("ATLASES", assetsFolder);

        setFillParent(true);
        buildUi();
    }

    private void buildUi() {
        Stack layers = new Stack();

        layers.add(createCenterActions());
        layers.add(createBottomActions());
        layers.add(createTopActions());

        add(layers).grow();
    }

    private Table createCenterActions() {
        Table layer = new Table();
        layer.center();

        Label title = new Label(
                "Choose Chapter",
                skin,
                "big_outline"
        );

        List<Chapter> chapters = App.getLevelManager()
                .getChapters()
                .stream()
                .filter(chapter -> chapter.getGameMode() == GameMode.ADVENTURE)
                .filter(chapter -> chapter.getMenuRegion() != null)
                .filter(chapter -> !chapter.getMenuRegion().isBlank())
                .toList();

        ChapterCarousel carousel = new ChapterCarousel(
                skin,
                textures,
                chapters,
                this::enterChapter
        );

        layer.add(title)
                .padTop(30f)
                .padBottom(10f)
                .row();

        layer.add(carousel)
                .width(720f)
                .height(285f);

        return layer;
    }

    private void enterChapter(String chapterId) {
        MenuState previousState = App.getMenuState();

        String result = controller.enterChapter(chapterId);

        if (App.getMenuState() == MenuState.LEVEL_SELECTION) {
            return;
        }

        if (App.getMenuState() == previousState
                && result != null
                && !result.isBlank()) {
            NotificationCenter.error(result);
        }
    }

    private Table createTopActions() {
        Table layer = new Table();
        layer.top().right();

        ImageButton greenhouseButton =
                new ImageButton(skin, "hud_zg");

        greenhouseButton.addListener(
                listener(this::openGreenhouse)
        );

        ImageButton collectionButton =
                new ImageButton(skin, "almanac");

        collectionButton.addListener(
                listener(this::openCollection)
        );

        TextButton leaderboardButton =
                createLeaderboardButton();

        leaderboardButton.addListener(
                listener(this::openLeaderboard)
        );

        layer.add(greenhouseButton)
                .size(72f, 72f)
                .padTop(18f)
                .padRight(10f);

        layer.add(collectionButton)
                .size(72f, 72f)
                .padTop(18f)
                .padRight(10f);

        layer.add(leaderboardButton)
                .size(72f, 72f)
                .padTop(18f)
                .padRight(18f);

        return layer;
    }

    private void openGreenhouse() {
        App.setMenuState(MenuState.GREENHOUSE);
    }

    private Table createBottomActions() {
        Table layer = new Table();
        layer.bottom();

        ImageButton travelLogButton = createBannerButton(
                "image_ui_mainmenu_mainmenu_content_downloading"
        );
        travelLogButton.addListener(listener(this::openShop));

        layer.add(createLabeledBanner(travelLogButton, "Shop"))
                .size(270f, 108f)
                .padBottom(24f);
        return layer;
    }

    private TextButton createLeaderboardButton() {
        TextButton button = new TextButton(
                "",
                skin,
                "brown"
        );

        button.clearChildren();

        Table podium = new Table();
        podium.bottom();

        addPodiumStep(podium, 14f);
        addPodiumStep(podium, 28f);
        addPodiumStep(podium, 20f);

        podium.setTouchable(Touchable.disabled);

        button.add(podium)
                .width(48f)
                .height(36f)
                .bottom();

        return button;
    }

    private void addPodiumStep(
            Table podium,
            float height
    ) {
        Image step = new Image(skin.newDrawable(
                "image_ui_dialog_asset_tint_rounded_box_9slice",
                new Color(0.96f, 0.72f, 0.14f, 1f)
        ));

        step.setTouchable(Touchable.disabled);

        podium.add(step)
                .width(14f)
                .height(height)
                .padRight(2f)
                .bottom();
    }

    private void openLeaderboard() {
        App.setMenuState(MenuState.LEADERBOARD);
    }

    private void openCollection() {
        App.setMenuState(MenuState.COLLECTION);
    }

    private void openShop() {
        App.setMenuState(MenuState.SHOP);
    }

    private ImageButton createBannerButton(String drawableName) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = skin.getDrawable(drawableName);
        style.imageDown = skin.newDrawable(
                drawableName,
                new Color(0.82f, 0.82f, 0.82f, 1f)
        );
        ImageButton button = new ImageButton(style);
        addHoverEffect(button);
        return button;
    }

    private void addHoverEffect(ImageButton button) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(
                    InputEvent event,
                    float x,
                    float y,
                    int pointer,
                    Actor fromActor
            ) {
                animateHoverIn(button);
            }

            @Override
            public void exit(
                    InputEvent event,
                    float x,
                    float y,
                    int pointer,
                    Actor toActor
            ) {
                animateHoverOut(button);
            }
        });
    }

    private void animateHoverIn(ImageButton button) {
        button.clearActions();
        button.setTransform(true);
        button.setOrigin(Align.center);

        button.addAction(parallel(
                color(
                        new Color(1f, 1f, 0.78f, 1f),
                        0.16f,
                        Interpolation.fade
                ),
                scaleTo(
                        1.06f,
                        1.06f,
                        0.16f,
                        Interpolation.smooth
                )
        ));
    }

    private void animateHoverOut(ImageButton button) {
        button.clearActions();

        button.addAction(parallel(
                color(
                        Color.WHITE,
                        0.16f,
                        Interpolation.fade
                ),
                scaleTo(
                        1f,
                        1f,
                        0.16f,
                        Interpolation.smooth
                )
        ));
    }

    private Stack createLabeledBanner(
            ImageButton button,
            String caption
    ) {
        Stack banner = new Stack();
        banner.add(button);

        Label label = new Label(caption, skin, "medium_outline");
        label.setTouchable(Touchable.disabled);

        Table captionLayer = new Table();
        captionLayer.bottom();
        captionLayer.add(label).padBottom(7f);
        captionLayer.setTouchable(Touchable.disabled);
        banner.add(captionLayer);
        return banner;
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
