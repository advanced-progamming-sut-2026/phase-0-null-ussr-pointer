package com.ussr.pvz.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.ussr.pvz.controller.RegisterController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.SecurityQuestion;
import com.ussr.pvz.model.dto.PickQuestionRequest;
import com.ussr.pvz.model.dto.RegisterRequest;
import com.ussr.pvz.model.dto.RegistrationResult;
import com.ussr.pvz.notification.NotificationCenter;
import java.util.Arrays;


public final class RegisterMenu extends FadingMenu {
    private final RegisterController controller;
    private final Skin skin;

    private TextField usernameField;
    private TextField nicknameField;
    private TextField emailField;
    private TextField passwordField;
    private TextField confirmPasswordField;
    private SelectBox<String> genderBox;
    private SelectBox<String> questionBox;
    private TextField answerField;
    private TextField confirmAnswerField;

    public RegisterMenu(Skin skin) {
        this.skin = skin;
        this.controller = new RegisterController();

        buildRegistrationForm();
    }

    private void buildRegistrationForm() {
        clearChildren();
        initializeRegistrationFields();

        Table form = createRegistrationForm();
        addRegistrationFields(form);
        addRegistrationButtons(form);

        add(form);
    }

    private void initializeRegistrationFields() {
        usernameField = new TextField("", skin);
        nicknameField = new TextField("", skin);
        emailField = new TextField("", skin);
        passwordField = createPasswordField();
        confirmPasswordField = createPasswordField();

        genderBox = new SelectBox<>(skin);
        genderBox.setItems("Male", "Female");
    }

    private TextField createPasswordField() {
        TextField field = new TextField("", skin);
        field.setPasswordMode(true);
        field.setPasswordCharacter('*');
        return field;
    }

    private Table createRegistrationForm() {
        Table form = new Table();

        form.setBackground(
                skin.getDrawable(
                        "image_ui_dialog_asset_dialogborder_10"
                )
        );

        form.pad(30f);

        form.add(new Label("Create Account", skin, "big_outline"))
                .colspan(2)
                .padBottom(20f)
                .row();

        return form;
    }

    private void addRegistrationFields(Table form) {
        addFieldPair(
                form,
                "Username",
                usernameField,
                "Nickname",
                nicknameField
        );

        addFieldPair(
                form,
                "Email",
                emailField,
                "Gender",
                genderBox
        );

        addFieldPair(
                form,
                "Password",
                passwordField,
                "Confirm Password",
                confirmPasswordField
        );
    }

    private void addRegistrationButtons(Table form) {
        TextButton registerButton =
                new TextButton("Register", skin, "green");

        TextButton loginButton =
                new TextButton(
                        "Already have an account? Login",
                        skin,
                        "brown"
                );

        form.add(registerButton)
                .colspan(2)
                .width(220f)
                .height(58f)
                .padTop(18f)
                .row();

        form.add(loginButton)
                .colspan(2)
                .width(320f)
                .height(52f)
                .padTop(8f)
                .row();

        attachRegistrationListeners(registerButton, loginButton);
    }

    private void attachRegistrationListeners(
            TextButton registerButton,
            TextButton loginButton
    ) {
        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitRegistration();
            }
        });

        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                App.setMenuState(MenuState.LOGIN);
            }
        });
    }

    private void addFieldPair(
            Table form,
            String leftTitle,
            Actor leftField,
            String rightTitle,
            Actor rightField
    ) {
        form.add(new Label(leftTitle, skin))
                .left()
                .padRight(12f);

        form.add(new Label(rightTitle, skin))
                .left()
                .row();

        form.add(leftField)
                .width(280f)
                .height(48f)
                .padRight(12f)
                .padBottom(12f);

        form.add(rightField)
                .width(280f)
                .height(48f)
                .padBottom(12f)
                .row();
    }

    private void submitRegistration() {
        RegisterRequest request = new RegisterRequest(
                usernameField.getText().trim(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                nicknameField.getText().trim(),
                emailField.getText().trim(),
                genderBox.getSelected()
        );

        RegistrationResult result =
                controller.register(request);

        switch (result.status()) {
            case DETAILS_ACCEPTED -> {
                NotificationCenter.success(result.message());
                transitionContent(this::buildSecurityQuestionForm);
            }

            case ERROR ->
                    NotificationCenter.error(result.message());

            case COMPLETED -> {
                // Registration cannot complete before security setup.
            }
        }
    }

    private void buildSecurityQuestionForm() {
        clearChildren();
        initializeSecurityFields();

        Table form = createSecurityForm();
        addSecurityInputs(form);
        addSecurityButtons(form);

        add(form);
    }

    private void initializeSecurityFields() {
        questionBox = new SelectBox<>(skin);

        String[] questions = Arrays.stream(SecurityQuestion.values())
                .map(SecurityQuestion::getText)
                .toArray(String[]::new);

        questionBox.setItems(questions);

        answerField = new TextField("", skin);
        confirmAnswerField = new TextField("", skin);
    }

    private Table createSecurityForm() {
        Table form = new Table();

        form.setBackground(
                skin.getDrawable(
                        "image_ui_dialog_asset_dialogborder_10"
                )
        );

        form.pad(30f);

        form.add(new Label(
                "Security Question",
                skin,
                "big_outline"
        )).colspan(2).padBottom(20f).row();

        return form;
    }

    private void addSecurityInputs(Table form) {
        addWideField(form, "Question", questionBox);
        addWideField(form, "Answer", answerField);
        addWideField(
                form,
                "Confirm Answer",
                confirmAnswerField
        );
    }

    private void addWideField(
            Table form,
            String title,
            Actor field
    ) {
        form.add(new Label(title, skin))
                .colspan(2)
                .left()
                .row();

        form.add(field)
                .colspan(2)
                .width(500f)
                .height(48f)
                .padBottom(12f)
                .row();
    }

    private void addSecurityButtons(Table form) {
        TextButton backButton =
                new TextButton("Back", skin, "brown");

        TextButton finishButton =
                new TextButton("Create Account", skin, "green");

        form.add(backButton)
                .width(180f)
                .height(58f)
                .padRight(10f);

        form.add(finishButton)
                .width(220f)
                .height(58f)
                .row();

        attachSecurityListeners(backButton, finishButton);
    }

    private void attachSecurityListeners(
            TextButton backButton,
            TextButton finishButton
    ) {
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                transitionContent(RegisterMenu.this::buildRegistrationForm);
            }
        });

        finishButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitSecurityQuestion(
                        questionBox,
                        answerField,
                        confirmAnswerField
                );
            }
        });
    }

    private void submitSecurityQuestion(
            SelectBox<String> questionBox,
            TextField answerField,
            TextField confirmAnswerField
    ) {
        PickQuestionRequest request = new PickQuestionRequest(
                Integer.toString(
                        questionBox.getSelectedIndex() + 1
                ),
                answerField.getText().trim(),
                confirmAnswerField.getText().trim()
        );

        RegistrationResult result =
                controller.pickQuestion(request);

        switch (result.status()) {
            case COMPLETED ->
                    NotificationCenter.success(result.message());

            case ERROR ->
                    NotificationCenter.error(result.message());

            case DETAILS_ACCEPTED -> {
                // Not possible during this phase.
            }
        }
    }
}
