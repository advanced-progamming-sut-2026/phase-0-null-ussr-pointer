package com.ussr.pvz.service;

import com.ussr.pvz.controller.command.ValidationRegex;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.*;
import com.ussr.pvz.model.dto.PickQuestionRequest;
import com.ussr.pvz.model.dto.RegisterRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterService {

    private AccountState pendingAccount;

    public String register(RegisterRequest request) {
        String validationError = validateRegistration(request);
        if (validationError != null) return validationError;

        Map<String, Integer> initialPlantMap = AdventureProgress.initializePlantsLvl();
        initialPlantMap.put("PEASHOOTER", 1);
        initialPlantMap.put("SUNFLOWER", 1);
        initialPlantMap.put("WALL-NUT", 1);
        initialPlantMap.put("POTATO MINE", 1);

        long now = System.currentTimeMillis();

        pendingAccount = new AccountState(
                request.username(),
                request.nickname(),
                request.password(),
                request.email(),
                Gender.from(request.gender()),
                3,         // default difficulty level
                null,  // securityQuestion — not picked yet
                null,  // securityAnswer — not picked yet
                1,     // starting chapter
                1,     // starting level
                0,     // minigames won
                0,     // completed quests
                0,     // coin
                0,     // gem
                0,     // score
                initialPlantMap,
                new ArrayList<>(),
                List.of(NewsItem.initialNews()),
                null,  // greenhouse
                null,  // saved boosts
                0,     // plantFoodCount
                new HashMap<>(),   //seedPackets
                new ArrayList<>(),
                new HashMap<>(),
                now,  // lastLoginTime
                now,
                new ArrayList<>()// lastDailyResetTime
        );

        StringBuilder sb = new StringBuilder("pick a security question:\n");
        for (SecurityQuestion q : SecurityQuestion.values()) {
            sb.append(q.ordinal() + 1).append(". ").append(q.getText()).append("\n");
        }
        return sb.toString().trim();
    }

    private String validateRegistration(RegisterRequest request) {
        if (usernameExists(request.username())) return "username already exists";
        if (!validUsername(request.username())) return "invalid username";
        if (!validPasswordLength(request.password())) return "invalid password length";
        if (!validPasswordLower(request.password())) return "password must contain a lowercase character";
        if (!validPasswordUpper(request.password())) return "password must contain an uppercase character";
        if (!validPasswordNumber(request.password())) return "password must contain a number";
        if (!validPasswordSpecific(request.password())) return "password must contain a specific character";
        if (!request.password().equals(request.passwordConfirm())) return "password confirm does not match to the password";
        if (!validEmail(request.email())) return "invalid email format";
        if (!validNickname(request.nickname())) return "invalid nickname length";
        if (!validGender(request.gender())) return "invalid gender";
        return null;
    }

    public String pickQuestion(PickQuestionRequest request) {
        if (pendingAccount == null) return "no pending registration";

        int questionNumber;
        try {
            questionNumber = Integer.parseInt(request.questionNumber());
        } catch (NumberFormatException e) {
            return "invalid question number";
        }

        SecurityQuestion[] questions = SecurityQuestion.values();
        if (questionNumber < 1 || questionNumber > questions.length) return "invalid question number";
        if (!request.answer().equals(request.answerConfirm())) return "answers are not identical";

        SecurityQuestion chosenQuestion = questions[questionNumber - 1];
        AccountState finalState = pendingAccount.finalizeRegistration(chosenQuestion, request.answer());
        App.addAccount(new Account(finalState, new Collection(List.of(), List.of())));

        List<AccountState> allUpdatedStates = App.getAccounts().stream().map(Account::toState).toList();
        SaveService.saveAccounts(allUpdatedStates);

        pendingAccount = null;
        App.setMenuState(MenuState.LOGIN);
        return "registered successfully";
    }

    private boolean usernameExists(String username) {
        return App.getAccounts().stream().anyMatch(a -> a.getName().equals(username));
    }

    private boolean validUsername(String username) {
        return ValidationRegex.VALID_USERNAME.matchToRegex(username).matches();
    }

    private boolean validPasswordLength(String password) {
        return ValidationRegex.VALID_PASSWORD_LENGTH.matchToRegex(password).matches();
    }

    private boolean validPasswordLower(String password) {
        return ValidationRegex.VALID_PASSWORD_LOWER.matchToRegex(password).matches();
    }

    private boolean validPasswordUpper(String password) {
        return ValidationRegex.VALID_PASSWORD_UPPER.matchToRegex(password).matches();
    }

    private boolean validPasswordSpecific(String password) {
        return ValidationRegex.VALID_PASSWORD_SPECIFIC_CHARACTER.matchToRegex(password).matches();
    }

    private boolean validPasswordNumber(String password) {
        return ValidationRegex.VALID_PASSWORD_NUMBER.matchToRegex(password).matches();
    }

    private boolean validNickname(String nickname) {
        return ValidationRegex.VALID_NICKNAME.matchToRegex(nickname).matches();
    }

    private boolean validEmail(String email) {
        return ValidationRegex.VALID_EMAIL.matchToRegex(email).matches();
    }

    private boolean validGender(String gender) {
        return ValidationRegex.VALID_GENDER.matchToRegex(gender).matches();
    }
}