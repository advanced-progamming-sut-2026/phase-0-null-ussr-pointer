package com.ussr.pvz.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.behavior.CouchIZombieBehavior;

import java.util.ArrayList;
import java.util.List;

public class KeyboardZombieInputWidget extends Actor {

    private static final int[] SLOT_KEYS = {
            Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3,
            Input.Keys.NUM_4, Input.Keys.NUM_5, Input.Keys.NUM_6,
            Input.Keys.NUM_7, Input.Keys.NUM_8, Input.Keys.NUM_9
    };

    private final GameplayController controller;
    private String selectedZombieKey;

    public KeyboardZombieInputWidget(GameplayController controller) {
        this.controller = controller;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!isCouchMatchActive()) {
            return;
        }

        handleCursorMovement();
        handleZombieSelection();
        handlePlacementConfirmation();
    }

    private boolean isCouchMatchActive() {
        GameSession session = App.getGameSession();
        return session != null
                && session.getLevel() != null
                && session.getLevel().getBehavior() instanceof CouchIZombieBehavior;
    }

    private void handleCursorMovement() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            controller.moveZombieCursor(-1, 0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            controller.moveZombieCursor(1, 0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            controller.moveZombieCursor(0, 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            controller.moveZombieCursor(0, -1);
        }
    }

    private void handleZombieSelection() {
        List<String> zombieKeys = allowedZombieKeys();

        for (int i = 0; i < SLOT_KEYS.length && i < zombieKeys.size(); i++) {
            if (!Gdx.input.isKeyJustPressed(SLOT_KEYS[i])) {
                continue;
            }

            String key = zombieKeys.get(i);
            selectedZombieKey = key.equals(selectedZombieKey) ? null : key;
            controller.setSelectedZombieKey(selectedZombieKey);
        }
    }

    private void handlePlacementConfirmation() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            controller.confirmZombiePlacement();
        }
    }

    private List<String> allowedZombieKeys() {
        List<String> keys = new ArrayList<>();

        GameSession session = App.getGameSession();
        if (session == null) return keys;

        Level level = session.getLevel();
        if (level == null || level.getAllowedZombies() == null) return keys;

        for (Level.AllowedZombie allowed : level.getAllowedZombies()) {
            if (!"SunProducerZombie".equalsIgnoreCase(allowed.id())) {
                keys.add(allowed.id());
            }
        }

        return keys;
    }
}