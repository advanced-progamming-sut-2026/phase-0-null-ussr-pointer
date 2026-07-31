package com.ussr.pvz.view.notification;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.ussr.pvz.notification.Notification;
import com.ussr.pvz.notification.NotificationCenter;
import com.ussr.pvz.notification.NotificationType;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;

public final class NotificationOverlay extends Table {
    private static final int MAX_VISIBLE = 4;

    private static final float TOAST_WIDTH = 390f;
    private static final float TOAST_HEIGHT = 54f;
    private static final float TOAST_SPACING = 4f;
    private static final float DROP_DISTANCE = 45f;

    private final Skin skin;
    private final Table notificationList;

    public NotificationOverlay(Skin skin) {
        this.skin = skin;
        this.notificationList = new Table();

        setFillParent(true);
        setTouchable(Touchable.disabled);
        top();

        add(notificationList)
                .expandX()
                .top()
                .padTop(2f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        Notification notification;

        while (notificationList.getChildren().size < MAX_VISIBLE
                && (notification = NotificationCenter.poll()) != null) {
            display(notification);
        }
    }

    private void display(Notification notification) {
        Label label = new Label(
                notification.text(),
                skin,
                "default"
        );

        label.setWrap(false);
        label.setEllipsis("...");
        label.setAlignment(Align.center);
        label.setColor(colorFor(notification.type()));

        Container<Label> toast = new Container<>(label);

        toast.setBackground(
                skin.getDrawable("image_ui_generic_brownbutton_10")
        );

        toast.pad(3f, 16f, 4f, 16f);
        toast.setTouchable(Touchable.disabled);
        toast.getColor().a = 0f;

        Table slot = new Table();
        slot.setTouchable(Touchable.disabled);
        slot.setClip(true);
        slot.addActor(toast);

        toast.setBounds(
                0f,
                DROP_DISTANCE,
                TOAST_WIDTH,
                TOAST_HEIGHT
        );

        Cell<Table> newCell = notificationList.add(slot)
                .width(TOAST_WIDTH)
                .height(TOAST_HEIGHT)
                .padBottom(TOAST_SPACING);

        newCell.row();

        notificationList.getCells().removeValue(newCell, true);
        notificationList.getCells().insert(0, newCell);
        notificationList.invalidateHierarchy();

        toast.addAction(sequence(
                parallel(
                        fadeIn(0.18f),
                        moveTo(
                                0f,
                                0f,
                                0.48f,
                                Interpolation.bounceOut
                        )
                ),
                delay(notification.durationSeconds()),
                fadeOut(0.3f),
                Actions.run(slot::remove)
        ));
    }

    private Color colorFor(NotificationType type) {
        return switch (type) {
            case SUCCESS ->
                    new Color(0.55f, 1f, 0.48f, 1f);

            case WARNING ->
                    new Color(1f, 0.88f, 0.3f, 1f);

            case ERROR ->
                    new Color(1f, 0.46f, 0.4f, 1f);

            case INFO ->
                    Color.WHITE;
        };
    }
}