package com.ussr.pvz.server.account;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AdventureProgress;
import com.ussr.pvz.model.account.Collection;
import com.ussr.pvz.shared.account.AccountState;
import com.ussr.pvz.shared.account.Gender;
import com.ussr.pvz.shared.account.NewsItem;
import com.ussr.pvz.shared.account.SecurityQuestion;
import com.ussr.pvz.shared.dto.PickQuestionRequest;
import com.ussr.pvz.shared.dto.RegisterRequest;
import com.ussr.pvz.shared.dto.RegistrationResult;
import com.ussr.pvz.shared.util.ValidationRegex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterService {

    private final AccountRepository accountRepository;

    public RegisterService(
            AccountRepository accountRepository
    ) {
        this.accountRepository = accountRepository;
    }

    public PendingRegistrationResult register(RegisterRequest request) {
        String validationError = validateRegistration(request);
        if (validationError != null) {
            return PendingRegistrationResult.error(validationError);
        }
        Map<String, Integer> initialPlantMap = AdventureProgress.initializePlantsLvl();
        initialPlantMap.put("PEASHOOTER", 1);
        initialPlantMap.put("SUNFLOWER", 1);
        initialPlantMap.put("WALL-NUT", 1);
        initialPlantMap.put("POTATO MINE", 1);
        initialPlantMap.put("ICEBERG LETTUCE", 1);
        initialPlantMap.put("GRAVE BUSTER", 1);
        long now = System.currentTimeMillis();
        AccountState pendingAccount =
                new AccountState(request.username(), request.nickname(), request.password(), request.email(),
                        Gender.from(request.gender()), 3, 1f, null,
                        null, 1, 1, 0, 0, 0, 0,
                        0, initialPlantMap, new ArrayList<>(),
                        List.of(NewsItem.initialNews()), null, null, 0,
                        new HashMap<>(),
                        new ArrayList<>(),
                        new HashMap<>(),
                        now,
                        now,
                        new ArrayList<>(),
                        0
                );

        return PendingRegistrationResult.success(
                RegistrationResult.detailsAccepted(
                        "Account details accepted. Choose a security question."
                ),
                pendingAccount
        );
    }

    public RegistrationResult pickQuestion(AccountState pendingAccount, PickQuestionRequest request) {
        if (pendingAccount == null) {
            return RegistrationResult.error("no pending registration");
        }
        int questionNumber;
        try {
            questionNumber = Integer.parseInt(request.questionNumber());
        } catch (NumberFormatException e) {
            return RegistrationResult.error("invalid question number");
        }
        SecurityQuestion[] questions = SecurityQuestion.values();
        if (questionNumber < 1 || questionNumber > questions.length) {
            return RegistrationResult.error("invalid question number");
        }
        if (!request.answer().equals(request.answerConfirm())) {
            return RegistrationResult.error("Security answers do not match.");
        }
        SecurityQuestion chosenQuestion = questions[questionNumber - 1];
        AccountState finalState = pendingAccount.finalizeRegistration(chosenQuestion, request.answer());
        Account account = new Account(finalState, new Collection(List.of(), List.of()));
        accountRepository.add(account);
        return RegistrationResult.completed("Registered successfully.");
    }

    private String validateRegistration(RegisterRequest request) {
        if (usernameExists(request.username())) {
            return "username already exists";
        }
        if (!validUsername(request.username())) {
            return "invalid username";
        }
        if (!validPasswordLength(request.password())) {
            return "invalid password length";
        }
        if (!validPasswordLower(request.password())) {
            return "password must contain a lowercase character";
        }
        if (!validPasswordUpper(request.password())) {
            return "password must contain an uppercase character";
        }
        if (!validPasswordNumber(request.password())) {
            return "password must contain a number";
        }
        if (!validPasswordSpecific(request.password())) {
            return "password must contain a specific character";
        }
        if (!request.password().equals(request.passwordConfirm())) {
            return "password confirm does not match to the password";
        }
        if (!validEmail(request.email())) {
            return "invalid email format";
        }
        if (!validNickname(request.nickname())) {
            return "invalid nickname length";
        }
        if (!validGender(request.gender())) {
            return "invalid gender";
        }
        return null;
    }

    private boolean usernameExists(String username) {
        return accountRepository.usernameExists(username);
    }

    private boolean validUsername(String username) {
        return ValidationRegex
                .VALID_USERNAME
                .matchToRegex(username)
                .matches();
    }

    private boolean validPasswordLength(
            String password
    ) {

        return ValidationRegex
                .VALID_PASSWORD_LENGTH
                .matchToRegex(password)
                .matches();
    }

    private boolean validPasswordLower(
            String password
    ) {

        return ValidationRegex
                .VALID_PASSWORD_LOWER
                .matchToRegex(password)
                .matches();
    }

    private boolean validPasswordUpper(
            String password
    ) {

        return ValidationRegex
                .VALID_PASSWORD_UPPER
                .matchToRegex(password)
                .matches();
    }

    private boolean validPasswordSpecific(
            String password
    ) {

        return ValidationRegex
                .VALID_PASSWORD_SPECIFIC_CHARACTER
                .matchToRegex(password)
                .matches();
    }

    private boolean validPasswordNumber(
            String password
    ) {

        return ValidationRegex
                .VALID_PASSWORD_NUMBER
                .matchToRegex(password)
                .matches();
    }

    private boolean validNickname(
            String nickname
    ) {

        return ValidationRegex
                .VALID_NICKNAME
                .matchToRegex(nickname)
                .matches();
    }

    private boolean validEmail(
            String email
    ) {

        return ValidationRegex
                .VALID_EMAIL
                .matchToRegex(email)
                .matches();
    }

    private boolean validGender(
            String gender
    ) {

        return ValidationRegex
                .VALID_GENDER
                .matchToRegex(gender)
                .matches();
    }
}