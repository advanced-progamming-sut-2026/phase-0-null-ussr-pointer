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

    /** PamActor draws around the centre of its default 80x80 actor bounds. */
    private static final float PAM_ACTOR_HALF_SIZE = 40f;
    private static final float WIND_PAM_CANVAS_SIZE = 390f;
    private static final float WIND_SCALE = 2.5f;

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
                LawnGridLayout.cellX(event.column())
                        + LawnGridLayout.CELL_WIDTH / 2f
                        - PAM_ACTOR_HALF_SIZE,
                LawnGridLayout.cellY(event.row())
                        + LawnGridLayout.CELL_HEIGHT / 2f
                        - PAM_ACTOR_HALF_SIZE
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

        actor.setPamScale(WIND_SCALE);
        actor.setLooping(false);
        actor.setTouchable(Touchable.disabled);

        float lawnLeft = LawnGridLayout.cellX(0);
        float lawnRight = LawnGridLayout.cellX(LawnGridLayout.COLUMNS);
        float windHalfWidth = WIND_PAM_CANVAS_SIZE * WIND_SCALE / 2f;

        // Actor coordinates are converted from desired PAM-centre positions.
        // Start and finish completely outside the lawn so the whole gust
        // visibly sweeps across every column.
        float startX = lawnRight + windHalfWidth - PAM_ACTOR_HALF_SIZE;
        float endX = lawnLeft - windHalfWidth - PAM_ACTOR_HALF_SIZE;

        float y = LawnGridLayout.cellY(0)
                + LawnGridLayout.ROWS * LawnGridLayout.CELL_HEIGHT / 2f
                - PAM_ACTOR_HALF_SIZE;

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
