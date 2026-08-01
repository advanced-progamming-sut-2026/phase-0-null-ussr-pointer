package com.ussr.pvz.view.mainmenu.profile;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.ussr.pvz.controller.maincontroller.ProfileController;

public final class ChangePasswordPanel extends Table {
    private final Skin skin;
    private final ProfileController controller;
    private final Runnable onFinished;

    private TextField oldPassword;
    private TextField newPassword;
    private TextField confirmation;

    public ChangePasswordPanel(
            Skin skin,
            ProfileController controller,
            Runnable onFinished
    ) {
        this.skin = skin;
        this.controller = controller;
        this.onFinished = onFinished;

        buildUi();
    }

    private void buildUi() {
        setBackground(skin.getDrawable(
                "image_ui_dialog_asset_inner_bkgd_10"
        ));

        pad(20f);

        add(ProfileUiFactory.sectionTitle(
                skin,
                "Change Password"
        )).colspan(2)
                .left()
                .padBottom(18f)
                .row();

        initializeFields();

        addPasswordField("Old Password", oldPassword);
        addPasswordField("New Password", newPassword);
        addPasswordField("Confirm Password", confirmation);
        addButtons();
    }

    private void initializeFields() {
        oldPassword = createPasswordField();
        newPassword = createPasswordField();
        confirmation = createPasswordField();
    }

    private TextField createPasswordField() {
        TextField field = new TextField("", skin);
        field.setPasswordMode(true);
        field.setPasswordCharacter('*');
        return field;
    }

    private void addPasswordField(
            String title,
            TextField field
    ) {
        add(ProfileUiFactory.cardText(skin, title))
                .colspan(2)
                .left()
                .row();

        add(field)
                .colspan(2)
                .width(400f)
                .height(48f)
                .padBottom(12f)
                .row();
    }

    private void addButtons() {
        TextButton cancel =
                new TextButton("Cancel", skin, "brown");

        TextButton save =
                new TextButton("Save Password", skin, "green");

        add(cancel)
                .width(180f)
                .height(52f)
                .padRight(8f);

        add(save)
                .width(220f)
                .height(52f)
                .row();

        cancel.addListener(
                ProfileUiFactory.listener(onFinished)
        );

        save.addListener(
                ProfileUiFactory.listener(this::submit)
        );
    }

    private void submit() {
        if (!newPassword.getText().equals(
                confirmation.getText()
        )) {
            ProfileUiFactory.showResult(
                    "passwords do not match"
            );
            return;
        }

        String result = controller.changePassword(
                oldPassword.getText(),
                newPassword.getText()
        );

        if (ProfileUiFactory.showResult(result)) {
            clearPasswordFields();
            onFinished.run();
        }
    }

    private void clearPasswordFields() {
        oldPassword.setText("");
        newPassword.setText("");
        confirmation.setText("");
    }
}