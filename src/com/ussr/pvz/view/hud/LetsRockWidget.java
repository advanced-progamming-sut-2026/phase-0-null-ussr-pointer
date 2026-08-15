package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.behavior.PlantWhatYouGetBehavior;

/**
 * "LET'S ROCK!" button shown during Plant-What-You-Get levels.
 * Visible only while waves have not yet been started by the player.
 * Sits top-left of the HUD, right beside the sun counter.
 */
public class LetsRockWidget extends TextButton {

    public LetsRockWidget(Skin skin) {
        super("LET'S ROCK!", skin, "green");

        getLabel().setFontScale(0.95f);
        getLabel().setColor(Color.WHITE);
        pad(6f, 14f, 6f, 14f);
        setTouchable(Touchable.enabled);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSession session = App.getGameSession();
                if (session != null && !session.isWavesStarted()) {
                    session.startWaves();
                }
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Show only when the current level uses PlantWhatYouGetBehavior
        // AND the player has not yet started the waves.
        GameSession session = App.getGameSession();
        if (session == null || session.isGameOver()) {
            setVisible(false);
            return;
        }

        Level level = session.getLevel();
        boolean isPlantWhatYouGet =
                level != null && level.getBehavior() instanceof PlantWhatYouGetBehavior;
        boolean wavesNotStarted = !session.isWavesStarted();

        setVisible(isPlantWhatYouGet && wavesNotStarted);
        setTouchable(isVisible() ? Touchable.enabled : Touchable.disabled);
    }
}