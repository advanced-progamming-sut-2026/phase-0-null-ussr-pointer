package com.ussr.pvz.view.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.notification.NotificationCenter;

/**
 * Invisible actor placed in the UI tree to monitor GameSession conditions
 * and push banners/toasts to the generic NotificationCenter system.
 */
public class GameEventAnnouncer extends Actor {
    private boolean started = false;
    private GameSession currentSession;

    public GameEventAnnouncer() {
        setTouchable(Touchable.disabled);
        setVisible(false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        if (session == null || session.getLevel() == null) {
            started = false;
            return;
        }

        if (session != currentSession) {
            started = false;
            currentSession = session;
        }

        if (!started) {
            NotificationCenter.info("Level Start: Defend your brains!");
            started = true;
        }
    }
}