package com.ussr.pvz.view;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public abstract class FadingMenu extends Table {
    private static final float FADE_DURATION = 0.25f;

    protected final void transitionContent(Runnable rebuild) {
        clearActions();
        setTouchable(Touchable.disabled);

        addAction(sequence(
                fadeOut(FADE_DURATION),
                run(() -> {
                    rebuild.run();
                    getColor().a = 0f;
                }),
                fadeIn(FADE_DURATION),
                run(() -> setTouchable(Touchable.childrenOnly))
        ));
    }
}
