package com.ussr.pvz.view.gameplay;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.event.GameEvent;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.PamPlayer;

public class StormRenderLayer extends Group {

    private static final String SANDSTORM_REAR_PAM =
            "768/INITIAL/EFFECTS/SANDSTORM_REAR/SANDSTORM_REAR.PAM";

    private static final String SANDSTORM_TOP_PAM =
            "768/INITIAL/EFFECTS/SANDSTORM_TOP/SANDSTORM_TOP.PAM";

    private static final String FREEZING_WIND_PAM =
            "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM";

    private final PamPlayer pamPlayer;
    private final boolean rearLayer;

    public StormRenderLayer(
            PamPlayer pamPlayer,
            boolean rearLayer
    ) {
        this.pamPlayer = pamPlayer;
        this.rearLayer = rearLayer;
        setTouchable(Touchable.disabled);

        GameSession session = App.getGameSession();

        if (session != null) {
            session.getEventBus().subscribe(
                    GameEvent.SandstormTriggered.class,
                    this::showSandstorm
            );

            if (!rearLayer) {
                session.getEventBus().subscribe(
                        GameEvent.FreezingWindTriggered.class,
                        event -> showFreezingWind()
                );
            }
        }
    }

    private void showSandstorm(
            GameEvent.SandstormTriggered event
    ) {
        String pamPath = rearLayer
                ? SANDSTORM_REAR_PAM
                : SANDSTORM_TOP_PAM;

        PamActor actor = new PamActor(
                pamPlayer,
                pamPath,
                "intro"
        );

        actor.setPamScale(1f);
        actor.setLooping(false);
        actor.setTouchable(Touchable.disabled);

        actor.setPosition(
                LawnGridLayout.cellX(event.column()),
                LawnGridLayout.cellY(event.row()) - 50f
        );

        addActor(actor);

        actor.addAction(
                Actions.sequence(
                        Actions.delay(0.34f),
                        Actions.run(() -> {
                            actor.setClip("loop");
                            actor.setLooping(true);
                        }),
                        Actions.delay(1.2f),
                        Actions.run(() -> {
                            actor.setClip("outro");
                            actor.setLooping(false);
                        }),
                        Actions.delay(0.34f),
                        Actions.removeActor()
                )
        );
    }

    private void showFreezingWind() {
        PamActor actor = new PamActor(
                pamPlayer,
                FREEZING_WIND_PAM,
                "animation"
        );

        actor.setPamScale(2.5f);
        actor.setLooping(false);
        actor.setTouchable(Touchable.disabled);

        float startX =
                LawnGridLayout.OFFSET_X
                        + LawnGridLayout.COLUMNS
                        * LawnGridLayout.CELL_WIDTH;

        float endX = LawnGridLayout.OFFSET_X;

        float y =
                LawnGridLayout.OFFSET_Y
                        + LawnGridLayout.ROWS
                        * LawnGridLayout.CELL_HEIGHT / 2f;

        actor.setPosition(startX, y);
        addActor(actor);

        actor.addAction(
                Actions.sequence(
                        Actions.moveTo(endX, y, 2.56f),
                        Actions.removeActor()
                )
        );
    }
}
