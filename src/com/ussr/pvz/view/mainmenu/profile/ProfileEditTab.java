package com.ussr.pvz.view.mainmenu.profile;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.ussr.pvz.controller.maincontroller.ProfileController;
import com.ussr.pvz.model.account.Account;

public final class ProfileEditTab extends Table {
    private final Skin skin;
    private final ProfileController controller;
    private final Runnable onProfileChanged;
    private final Runnable onPasswordRequested;

    private final Table contentRoot;

    private TextField usernameField;
    private TextField nicknameField;
    private TextField emailField;

    public ProfileEditTab(
            Skin skin,
            ProfileController controller,
            Runnable onProfileChanged,
            Runnable onPasswordRequested
    ) {
        this.skin = skin;
        this.controller = controller;
        this.onProfileChanged = onProfileChanged;
        this.onPasswordRequested = onPasswordRequested;
        this.contentRoot = new Table();

        initializeFields();
        add(contentRoot).grow();
        showEditCards();
    }

    private void initializeFields() {
        usernameField = new TextField("", skin);
        nicknameField = new TextField("", skin);
        emailField = new TextField("", skin);
    }

    public void refresh(Account account) {
        if (account == null) {
            return;
        }

        usernameField.setText(account.getName());
        nicknameField.setText(account.getNickname());
        emailField.setText(account.getEmail());
    }

    private void showEditCards() {
        contentRoot.clearChildren();

        Table detailsCard = createDetailsCard();
        Table securityCard = createSecurityCard();

        contentRoot.add(detailsCard)
                .width(430f)
                .top()
                .padRight(15f);

        contentRoot.add(securityCard)
                .width(260f)
                .top();
    }

    private Table createDetailsCard() {
        Table card = ProfileUiFactory.card(
                skin,
                "Account Details"
        );

        addEditableRow(
                card,
                "Username",
                usernameField,
                this::submitUsername
        );

        addEditableRow(
                card,
                "Nickname",
                nicknameField,
                this::submitNickname
        );

        addEditableRow(
                card,
                "Email",
                emailField,
                this::submitEmail
        );

        return card;
    }

    private Table createSecurityCard() {
        Table card = ProfileUiFactory.card(
                skin,
                "Security"
        );

        Label description = ProfileUiFactory.cardText(
                skin,
                "Use your old password to set a new one."
        );

        description.setWrap(true);

        TextButton button = new TextButton(
                "Change Password",
                skin,
                "brown"
        );

        button.addListener(ProfileUiFactory.listener(
                onPasswordRequested
        ));

        card.add(description)
                .colspan(2)
                .width(210f)
                .padBottom(15f)
                .row();

        card.add(button)
                .colspan(2)
                .width(210f)
                .height(52f)
                .row();

        return card;
    }

    private void addEditableRow(
            Table table,
            String title,
            TextField field,
            Runnable action
    ) {
        table.add(ProfileUiFactory.cardText(skin, title))
                .colspan(2)
                .left()
                .row();

        table.add(field)
                .width(260f)
                .height(48f)
                .padBottom(12f);

        TextButton button =
                new TextButton("Change", skin, "green");

        button.addListener(
                ProfileUiFactory.listener(action)
        );

        table.add(button)
                .width(120f)
                .height(48f)
                .padLeft(8f)
                .padBottom(12f)
                .row();
    }

    private void submitUsername() {
        String result = controller.changeUsername(
                usernameField.getText()
        );

        handleProfileResult(result);
    }

    private void submitNickname() {
        String result = controller.changeNickname(
                nicknameField.getText()
        );

        handleProfileResult(result);
    }

    private void submitEmail() {
        String result = controller.changeEmail(
                emailField.getText()
        );

        handleProfileResult(result);
    }

    private void handleProfileResult(String result) {
        if (ProfileUiFactory.showResult(result)) {
            onProfileChanged.run();
        }
    }
}
