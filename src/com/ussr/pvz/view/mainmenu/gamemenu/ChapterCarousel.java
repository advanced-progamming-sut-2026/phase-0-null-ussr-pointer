package com.ussr.pvz.view.mainmenu.gamemenu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.model.level.Chapter;
import com.ussr.pvz.model.App;
import pvz.libpvz.textures.TextureBank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class ChapterCarousel extends Group {
    private static final float CARD_WIDTH = 180f;
    private static final float CARD_HEIGHT = 285f;
    private static final float TURN_TIME = 0.28f;

    private final Skin skin;
    private final TextureBank textures;
    private final Consumer<String> selectionHandler;
    private final List<Chapter> chapters;
    private final Map<Integer, Table> visibleCards = new HashMap<>();
    private int selectedIndex;
    private boolean turning;

    public ChapterCarousel(Skin skin, TextureBank textures,
                           List<Chapter> chapters,
                           Consumer<String> selectionHandler) {
        this.skin = skin;
        this.textures = textures;
        if (chapters == null || chapters.isEmpty()) {
            throw new IllegalArgumentException("At least one chapter is required");
        }
        this.chapters = List.copyOf(chapters);
        this.selectionHandler = selectionHandler;
        setSize(720f, 285f);
        addNavigationButtons();
        addInputNavigation();
        placeInitialCards();
    }

    private void addNavigationButtons() {
        TextButton previous = new TextButton("<", skin, "brown");
        TextButton next = new TextButton(">", skin, "brown");
        previous.setBounds(0f, 105f, 48f, 58f);
        next.setBounds(getWidth() - 48f, 105f, 48f, 58f);
        previous.addListener(listener(() -> turn(-1)));
        next.addListener(listener(() -> turn(1)));
        addActor(previous);
        addActor(next);
    }

    private void addInputNavigation() {
        addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y,
                                    float amountX, float amountY) {
                turn((amountY > 0f || amountX > 0f) ? 1 : -1);
                return true;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT) {
                    turn(keycode == Input.Keys.LEFT ? -1 : 1);
                    return true;
                }
                return false;
            }
        });
    }

    private void turn(int direction) {
        if (turning) return;
        turning = true;
        selectedIndex = wrap(selectedIndex + direction);
        animateCards(direction);
        addAction(sequence(delay(TURN_TIME), run(() -> turning = false)));
    }

    private void animateCards(int direction) {
        int previous = wrap(selectedIndex - 1);
        int next = wrap(selectedIndex + 1);
        removeHiddenCards(previous, selectedIndex, next);
        animateCard(previous, -1, direction);
        animateCard(selectedIndex, 0, direction);
        animateCard(next, 1, direction);
    }

    private void removeHiddenCards(int previous, int current, int next) {
        visibleCards.entrySet().removeIf(entry -> {
            if (entry.getKey() == previous || entry.getKey() == current
                    || entry.getKey() == next) return false;
            Table hiddenCard = entry.getValue();
            hiddenCard.addAction(sequence(
                    fadeOut(TURN_TIME),
                    run(hiddenCard::remove)
            ));
            return true;
        });
    }

    private void animateCard(int index, int position, int direction) {
        Table card = visibleCards.get(index);
        if (card == null) {
            card = createCard(index);
            card.setPosition(direction > 0 ? getWidth() : -CARD_WIDTH, sideY());
            card.setScale(0.55f);
            card.getColor().a = 0f;
            addActor(card);
            visibleCards.put(index, card);
        }
        float scale = position == 0 ? 1f : 0.68f;
        float alpha = position == 0 ? 1f : 0.38f;
        card.clearActions();
        card.addAction(parallel(
                moveTo(targetX(position), targetY(position), TURN_TIME,
                        Interpolation.smooth),
                scaleTo(scale, scale, TURN_TIME, Interpolation.smooth),
                alpha(alpha, TURN_TIME, Interpolation.fade)
        ));
    }

    private void placeInitialCards() {
        placeCard(wrap(selectedIndex - 1), -1);
        placeCard(selectedIndex, 0);
        placeCard(wrap(selectedIndex + 1), 1);
    }

    private void placeCard(int index, int position) {
        Table card = createCard(index);
        float scale = position == 0 ? 1f : 0.68f;
        card.setPosition(targetX(position), targetY(position));
        card.setScale(scale);
        card.getColor().a = position == 0 ? 1f : 0.38f;
        addActor(card);
        visibleCards.put(index, card);
    }

    private Table createCard(int index) {
        Chapter chapter = chapters.get(index);
        boolean unlocked = isUnlocked(chapter);
        Table card = new Table();
        card.setSize(CARD_WIDTH, CARD_HEIGHT);
        card.setTransform(true);
        card.setOrigin(Align.center);
        ImageButton image = createImageButton(chapter.getMenuRegion(), unlocked);
        image.addListener(listener(() -> clickCard(index)));
        Label title = new Label(chapter.getName(), skin, "medium_outline");
        title.setAlignment(Align.center);
        title.setWrap(true);
        title.setTouchable(Touchable.disabled);
        card.add(image).width(170f).height(205f).row();
        card.add(title).width(180f).height(40f).padTop(4f);

        if (!unlocked) {
            Label locked = new Label("LOCKED", skin);
            locked.setColor(Color.LIGHT_GRAY);
            locked.setTouchable(Touchable.disabled);
            card.row();
            card.add(locked).padTop(2f);
        }

        card.row();
        card.add(createProgressLabel(chapter)).padTop(6f);

        return card;
    }

    private Label createProgressLabel(Chapter chapter) {
        int totalLevels = chapter.getLevels().size();
        int completedLevels = countCompletedLevels(chapter);

        Label progress = new Label(
                completedLevels + "/" + totalLevels, skin
        );
        progress.setColor(Color.LIGHT_GRAY);
        progress.setTouchable(Touchable.disabled);
        return progress;
    }

    private int countCompletedLevels(Chapter chapter) {
        if (App.getAccount() == null
                || App.getAccount().getAdventureProgress() == null) {
            return 0;
        }

        long completed = chapter.getLevels().stream()
                .filter(level -> App.getAccount().getAdventureProgress()
                        .isLevelCompleted(level.getId()))
                .count();

        return (int) completed;
    }

    private ImageButton createImageButton(String regionName, boolean unlocked) {
        TextureRegion region = textures.region(regionName);
        if (region == null) {
            throw new IllegalArgumentException("Atlas region not found: " + regionName);
        }
        TextureRegionDrawable source = new TextureRegionDrawable(region);
        Drawable normal = unlocked
                ? source
                : source.tint(new Color(0.38f, 0.38f, 0.38f, 0.72f));
        Drawable pressed = source.tint(new Color(0.8f, 0.8f, 0.8f, 1f));
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = normal;
        style.imageDown = pressed;
        ImageButton button = new ImageButton(style);
        button.getImage().setScaling(Scaling.fit);
        return button;
    }

    private boolean isUnlocked(Chapter chapter) {
        return App.getAccount() != null
                && App.getAccount().getAdventureProgress() != null
                && App.getAccount().getAdventureProgress().isChapterUnlocked(
                chapter,
                App.getLevelManager().getChapters()
        );
    }

    private void clickCard(int index) {
        if (index == selectedIndex) {
            selectionHandler.accept(chapters.get(index).getId());
        } else if (index == wrap(selectedIndex - 1)) {
            turn(-1);
        } else if (index == wrap(selectedIndex + 1)) {
            turn(1);
        }
    }

    private float targetX(int position) {
        if (position < 0) return 80f;
        if (position > 0) return getWidth() - CARD_WIDTH - 80f;
        return (getWidth() - CARD_WIDTH) / 2f;
    }

    private float targetY(int position) {
        return position == 0 ? 18f : sideY();
    }

    private float sideY() {
        return 42f;
    }

    private int wrap(int index) {
        return (index + chapters.size()) % chapters.size();
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