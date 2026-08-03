package com.ussr.pvz.view.loading;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.ussr.pvz.view.animation.PamActor;
import pvz.libpvz.pam.PamPlayer;

public final class LoadingFlower extends Stack {
    private static final String BACK_PATH =
            "768/INITIAL/EFFECTS/"
                    + "LOAD_ICON_BACK/LOAD_ICON_BACK.PAM";

    private static final String FRONT_PATH =
            "768/INITIAL/EFFECTS/"
                    + "LOAD_ICON_FRONT/LOAD_ICON_FRONT.PAM";

    private final PamActor back;
    private final PamActor front;

    public LoadingFlower(PamPlayer pamPlayer) {
        back = createLayer(pamPlayer, BACK_PATH);
        front = createLayer(pamPlayer, FRONT_PATH);

        add(back);
        add(front);

        setTouchable(Touchable.disabled);
    }

    private PamActor createLayer(
            PamPlayer pamPlayer,
            String path
    ) {
        PamActor actor = new PamActor(
                pamPlayer,
                path,
                "animation"
        );

        actor.setPamScale(0.45f);
        actor.setTouchable(Touchable.disabled);
        return actor;
    }

    public void restart() {
        back.resetAnimation();
        front.resetAnimation();
    }
}
