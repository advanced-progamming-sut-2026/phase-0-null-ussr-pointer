package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.engine.session.GameSession;
import pvz.libpvz.textures.TextureBank;

/** Displays distinct, persistent victory and defeat actions. */
public class GameOverOverlay extends Table {
    private static final float DIALOG_WIDTH = 680f;
    private static final float DIALOG_HEIGHT = 480f;
    private static final String BUTTON_DRAWABLE =
            "image_ui_generic_brownbutton_10";
    private static final String NEXT_ICON =
            "IMAGE_UI_ALMANAC_STATS_SCREEN_NAV_ARROW_NEXT";
    private static final String NEXT_ICON_DOWN =
            null;
    private static final String REPLAY_ICON =
            "IMAGE_UI_HUD_WORLDMAP_BUTTONS_HUD_BACK_NORMAL";
    private static final String REPLAY_ICON_DOWN =
            "IMAGE_UI_HUD_WORLDMAP_BUTTONS_HUD_BACK_SELECTED";
    private static final String MENU_ICON =
            "IMAGE_UI_HUD_WORLDMAP_BUTTONS_HUD_MENU_NORMAL";
    private static final String MENU_ICON_DOWN =
            "IMAGE_UI_HUD_WORLDMAP_BUTTONS_HUD_MENU_SELECTED";

    private final Label titleLabel;
    private final Label messageLabel;
    private final Table victoryActions;
    private final Table defeatActions;
    private final Actor nextLevelButton;
    private Boolean showingVictory;

    public GameOverOverlay(Skin skin, TextureBank textures) {
        setFillParent(true);
        setTouchable(Touchable.enabled);
        setBackground(skin.newDrawable(
                "white-pixel",
                new Color(0f, 0f, 0f, 0.82f)
        ));

        Table dialog = new Table();
        if (skin.has(
                "image_ui_dialog_asset_dialogborder_10",
                Drawable.class
        )) {
            dialog.setBackground(skin.getDrawable(
                    "image_ui_dialog_asset_dialogborder_10"
            ));
        }
        dialog.pad(58f, 64f, 52f, 64f);

        titleLabel = new Label("GAME OVER", skin, "big_outline");
        messageLabel = new Label("", skin);
        messageLabel.setColor(new Color(0.95f, 0.9f, 0.7f, 1f));

        dialog.add(titleLabel).padBottom(18f).row();
        dialog.add(messageLabel).padBottom(34f).row();

        victoryActions = buildVictoryActions(skin, textures);
        defeatActions = buildDefeatActions(skin, textures);
        nextLevelButton = victoryActions.getChildren().first();

        Stack actionStack = new Stack();
        actionStack.add(victoryActions);
        actionStack.add(defeatActions);
        dialog.add(actionStack).growX().height(210f).row();

        add(dialog).width(DIALOG_WIDTH).height(DIALOG_HEIGHT);
        setVisible(false);
    }

    private Table buildVictoryActions(Skin skin, TextureBank textures) {
        Table actions = new Table();
        actions.defaults().width(180f).height(145f).pad(8f);
        actions.add(createImageActionButton(
                skin,
                textures,
                "Next Level",
                NEXT_ICON,
                NEXT_ICON_DOWN,
                this::goToNextLevel
        ));
        actions.add(createImageActionButton(
                skin,
                textures,
                "Replay Level",
                REPLAY_ICON,
                REPLAY_ICON_DOWN,
                this::replayLevel
        ));
        actions.add(createImageActionButton(
                skin,
                textures,
                "Game Menu",
                MENU_ICON,
                MENU_ICON_DOWN,
                this::goToGameMenu
        ));
        return actions;
    }

    private Table buildDefeatActions(Skin skin, TextureBank textures) {
        Table actions = new Table();
        actions.defaults().width(190f).height(145f).pad(10f);
        actions.add(createImageActionButton(
                skin,
                textures,
                "Retry",
                REPLAY_ICON,
                REPLAY_ICON_DOWN,
                this::replayLevel
        ));
        actions.add(createImageActionButton(
                skin,
                textures,
                "Exit to Map",
                MENU_ICON,
                MENU_ICON_DOWN,
                this::exitToMap
        ));
        return actions;
    }

    private Actor createImageActionButton(
            Skin skin,
            TextureBank textures,
            String text,
            String iconKey,
            String pressedIconKey,
            Runnable action
    ) {
        Drawable background = skin.has(BUTTON_DRAWABLE, Drawable.class)
                ? skin.getDrawable(BUTTON_DRAWABLE)
                : skin.getDrawable("white-pixel");
        Drawable backgroundDown = skin.newDrawable(
                background,
                new Color(0.76f, 0.76f, 0.76f, 1f)
        );
        Drawable icon = textures.region(iconKey) != null
                ? new TextureRegionDrawable(textures.region(iconKey))
                : skin.getDrawable("white-pixel");
        Drawable pressedIcon = pressedIconKey != null
                && textures.region(pressedIconKey) != null
                ? new TextureRegionDrawable(textures.region(pressedIconKey))
                : skin.newDrawable(
                        icon,
                        new Color(0.8f, 0.8f, 0.8f, 1f)
                );

        ImageButton.ImageButtonStyle style =
                new ImageButton.ImageButtonStyle();
        style.up = background;
        style.down = backgroundDown;
        style.imageUp = icon;
        style.imageDown = pressedIcon;

        ImageButton button = new ImageButton(style);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });

        Label label = new Label(text, skin, "big_outline");
        label.setTouchable(Touchable.disabled);

        Table result = new Table();
        result.add(button).width(108f).height(92f).row();
        result.add(label).padTop(4f);
        return result;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        // Wait until the post-game dialogue has finished before showing
        if (session == null || !session.isGameOver() || !session.isOutroShown()) {
            setVisible(false);
            return;
        }

        updateOutcomeLayout(session);
        setVisible(true);
    }

    private void updateOutcomeLayout(GameSession session) {
        boolean victory = session.isVictory();
        if (showingVictory != null && showingVictory == victory) {
            return;
        }

        showingVictory = victory;
        titleLabel.setText(victory ? "VICTORY!" : "GAME OVER");
        titleLabel.setColor(victory
                ? new Color(1f, 0.85f, 0.2f, 1f)
                : new Color(1f, 0.4f, 0.4f, 1f));
        messageLabel.setText(victory
                ? "Level complete! Choose your next move."
                : "The zombies reached your house.");

        victoryActions.setVisible(victory);
        defeatActions.setVisible(!victory);
        nextLevelButton.setVisible(victory && hasPreparedNextLevel(session));
    }

    private boolean hasPreparedNextLevel(GameSession session) {
        return App.getLevelManager().getCurrentLevel() != null
                && !App.getLevelManager().getCurrentLevel().getId()
                .equals(session.getLevel().getId());
    }

    private void goToNextLevel() {
        GameSession session = App.getGameSession();
        if (session == null || !hasPreparedNextLevel(session)) {
            return;
        }

        App.setGameSession(null);
        App.setMenuState(MenuState.CHOOSE_PLANT);
    }

    private void replayLevel() {
        GameSession session = App.getGameSession();
        if (session == null || session.getLevel() == null) {
            return;
        }

        String chapterId = session.getLevel().getChapter();
        String levelId = session.getLevel().getId();

        App.getLevelManager().startChapter(chapterId);
        App.getLevelManager().startLevel(levelId);
        App.setGameSession(null);
        App.setMenuState(MenuState.CHOOSE_PLANT);
    }

    private void goToGameMenu() {
        App.setGameSession(null);
        App.setMenuState(MenuState.GAME);
    }

    private void exitToMap() {
        App.setGameSession(null);
        App.setMenuState(MenuState.LEVEL_SELECTION);
    }
}
