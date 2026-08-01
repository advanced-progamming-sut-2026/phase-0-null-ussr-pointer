package com.ussr.pvz.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.controller.maincontroller.ProfileController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.notification.NotificationCenter;

public class ProfileMenu extends Table {
    private final ProfileController profileController = new ProfileController();

    private final Skin skin;

    private Label usernameValue;
    private Label nicknameValue;
    private Label emailValue;
    private Label genderValue;
    private Label completedLevelsValue;
    private Label scoreValue;

    private TextField usernameField;
    private TextField nicknameField;
    private TextField emailField;

    public ProfileMenu(Skin skin) {
        this.skin = skin;
        setFillParent(true);
        buildUi();
    }

    private void buildUi() {
        clearChildren();

        Account account = App.getAccount();
        if (account == null) {
            add(new Label("No user is logged in.", this.skin));
            return;
        }

        initializeFields(account);

        Table panel = createPanel();
        panel.add(createInformationSection())
                .top()
                .padRight(25f);

        panel.add(createEditingSection())
                .top();

        add(panel);
        refreshInformation();
    }

    private void initializeFields(Account account) {
        usernameValue = new Label("", skin);
        nicknameValue = new Label("", skin);
        emailValue = new Label("", skin);
        genderValue = new Label("", skin);
        completedLevelsValue = new Label("", skin);
        scoreValue = new Label("", skin);

        usernameField = new TextField(account.getName(), skin);
        nicknameField = new TextField(account.getNickname(), skin);
        emailField = new TextField(account.getEmail(), skin);
    }

    private Table createPanel() {
        Table panel = new Table();

        panel.setBackground(
                skin.getDrawable(
                        "image_ui_dialog_asset_dialogborder_10"
                )
        );

        panel.pad(30f);

        panel.add(new Label("Profile", skin, "big_outline"))
                .colspan(2)
                .padBottom(20f)
                .row();

        return panel;
    }

    private Table createInformationSection() {
        Table info = new Table();

        addInformationRow(info, "Username", usernameValue);
        addInformationRow(info, "Nickname", nicknameValue);
        addInformationRow(info, "Email", emailValue);
        addInformationRow(info, "Gender", genderValue);
        addInformationRow(
                info,
                "Completed Levels",
                completedLevelsValue
        );
        addInformationRow(info, "Meow Points", scoreValue);

        return info;
    }

    private void addInformationRow(
            Table table,
            String title,
            Label value
    ) {
        table.add(new Label(title + ":", skin))
                .left()
                .padRight(15f)
                .padBottom(10f);

        table.add(value)
                .left()
                .padBottom(10f)
                .row();
    }

    private Table createEditingSection() {
        Table editing = new Table();

        addEditableRow(
                editing,
                "Username",
                usernameField,
                "Change",
                this::submitUsername
        );

        addEditableRow(
                editing,
                "Nickname",
                nicknameField,
                "Change",
                this::submitNickname
        );

        addEditableRow(
                editing,
                "Email",
                emailField,
                "Change",
                this::submitEmail
        );

        TextButton passwordButton =
                new TextButton("Change Password", skin, "brown");

        editing.add(passwordButton)
                .colspan(2)
                .width(250f)
                .height(52f)
                .padTop(15f)
                .row();

        passwordButton.addListener(
                listener(this::showPasswordForm)
        );

        return editing;
    }

    private void showPasswordForm() {
        clearChildren();

        TextField oldPassword = createPasswordField();
        TextField newPassword = createPasswordField();
        TextField confirmation = createPasswordField();

        Table panel = createPasswordPanel();
        addPasswordField(panel, "Old Password", oldPassword);
        addPasswordField(panel, "New Password", newPassword);
        addPasswordField(panel, "Confirm Password", confirmation);
        addPasswordButtons(
                panel,
                oldPassword,
                newPassword,
                confirmation
        );

        add(panel);
    }

    private TextField createPasswordField() {
        TextField field = new TextField("", skin);
        field.setPasswordMode(true);
        field.setPasswordCharacter('*');
        return field;
    }

    private Table createPasswordPanel() {
        Table panel = new Table();

        panel.setBackground(
                skin.getDrawable(
                        "image_ui_dialog_asset_dialogborder_10"
                )
        );

        panel.pad(30f);

        panel.add(new Label(
                "Change Password",
                skin,
                "big_outline"
        )).colspan(2).padBottom(20f).row();

        return panel;
    }

    private void addPasswordField(
            Table panel,
            String title,
            TextField field
    ) {
        panel.add(new Label(title, skin))
                .colspan(2)
                .left()
                .row();

        panel.add(field)
                .colspan(2)
                .width(400f)
                .height(48f)
                .padBottom(12f)
                .row();
    }

    private void addPasswordButtons(
            Table panel,
            TextField oldPassword,
            TextField newPassword,
            TextField confirmation
    ) {
        TextButton cancel =
                new TextButton("Cancel", skin, "brown");

        TextButton save =
                new TextButton("Save Password", skin, "green");

        panel.add(cancel)
                .width(180f)
                .height(52f)
                .padRight(8f);

        panel.add(save)
                .width(220f)
                .height(52f)
                .row();

        cancel.addListener(listener(this::buildUi));

        save.addListener(listener(() -> submitPassword(
                oldPassword,
                newPassword,
                confirmation
        )));
    }

    private void submitPassword(
            TextField oldPassword,
            TextField newPassword,
            TextField confirmation
    ) {
        if (!newPassword.getText().equals(
                confirmation.getText()
        )) {
            NotificationCenter.error(
                    "Passwords do not match."
            );
            return;
        }

        String result = profileController.changePassword(
                oldPassword.getText(),
                newPassword.getText()
        );

        handleResult(result);

        if (result.endsWith("successfully")) {
            buildUi();
        }
    }

    private void addEditableRow(
            Table table,
            String title,
            TextField field,
            String buttonText,
            Runnable action
    ) {
        table.add(new Label(title, skin))
                .colspan(2)
                .left()
                .row();

        table.add(field)
                .width(260f)
                .height(48f)
                .padBottom(12f);

        TextButton button =
                new TextButton(buttonText, skin, "green");

        table.add(button)
                .width(120f)
                .height(48f)
                .padLeft(8f)
                .padBottom(12f)
                .row();

        button.addListener(listener(action));
    }

    private ChangeListener listener(Runnable action) {
        return new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        };
    }

    private void submitUsername() {
        handleResult(profileController.changeUsername(
                usernameField.getText()
        ));
    }

    private void submitNickname() {
        handleResult(profileController.changeNickname(
                nicknameField.getText()
        ));
    }

    private void submitEmail() {
        handleResult(profileController.changeEmail(
                emailField.getText()
        ));
    }

    private void handleResult(String result) {
        if (result.endsWith("successfully")) {
            NotificationCenter.success(result);
            refreshInformation();
        } else {
            NotificationCenter.error(result);
        }
    }

    private void refreshInformation() {
        Account account = App.getAccount();

        usernameValue.setText(account.getName());
        nicknameValue.setText(account.getNickname());
        emailValue.setText(account.getEmail());
        genderValue.setText(
                account.getGender().name().toLowerCase()
        );

        completedLevelsValue.setText(
                account.getAdventureProgress()
                        .getCompletedLevels()
                        .size()
        );

        scoreValue.setText(
                account.getScoreRecord().getScore()
        );
    }
}
