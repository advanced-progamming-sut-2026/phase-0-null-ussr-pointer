package com.ussr.pvz.server.account;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.shared.dto.AnswerRequest;
import com.ussr.pvz.shared.dto.ForgetPasswordRequest;
import com.ussr.pvz.shared.dto.LoginRequest;
import com.ussr.pvz.shared.dto.LoginResult;
import com.ussr.pvz.shared.dto.enums.LoginStatus;
import com.ussr.pvz.shared.util.SecurityUtil;
import com.ussr.pvz.shared.util.ValidationRegex;

public class LoginService {

    private final AccountRepository accountRepository;
    private final ServerSessionManager sessionManager;

    public LoginService(
            AccountRepository accountRepository,
            ServerSessionManager sessionManager
    ) {
        this.accountRepository = accountRepository;
        this.sessionManager = sessionManager;
    }

    public ServerLoginResult login(LoginRequest request) {

        Account account =
                accountRepository.findByUsername(
                        request.username()
                );

        if (account == null) {
            return ServerLoginResult.error(
                    LoginResult.error(
                            "Username not found."
                    )
            );
        }

        String hashedPassword =
                SecurityUtil.hashPassword(
                        request.password()
                );

        if (!account.getPassword()
                .equals(hashedPassword)) {

            return ServerLoginResult.error(
                    LoginResult.error(
                            "Invalid password."
                    )
            );
        }

        account.updateLoginTime();

        accountRepository.save();

        String token =
                sessionManager.createSession(
                        account
                );

        LoginResult result =
                LoginResult.of(
                        LoginStatus.LOGIN_SUCCESS,
                        "Logged in successfully."
                );

        return ServerLoginResult.success(
                result,
                token,
                account
        );
    }

    public void logout(String token) {
        sessionManager.removeSession(token);
    }

    public PasswordResetStartResult forgetPassword(
            ForgetPasswordRequest request
    ) {

        Account account =
                accountRepository.findByUsername(
                        request.username()
                );

        if (account == null) {
            return PasswordResetStartResult.error(
                    "Username not found."
            );
        }

        if (!account.getEmail()
                .equals(request.email())) {

            return PasswordResetStartResult.error(
                    "Invalid email."
            );
        }

        if (account.getSecurityQuestion() == null) {

            return PasswordResetStartResult.error(
                    "This account has no security question."
            );
        }

        String question =
                account.getSecurityQuestion()
                        .getText();

        return PasswordResetStartResult.success(
                question,
                account
        );
    }

    public LoginResult answer(
            Account pendingPasswordReset,
            AnswerRequest request
    ) {

        if (pendingPasswordReset == null) {

            return LoginResult.error(
                    "No active password reset."
            );
        }

        if (!pendingPasswordReset
                .getSecurityAnswer()
                .equals(request.answer())) {

            return LoginResult.error(
                    "Wrong security answer."
            );
        }

        return LoginResult.of(
                LoginStatus.ANSWER_ACCEPTED,
                "Answer accepted."
        );
    }

    public LoginResult resetPassword(
            Account pendingPasswordReset,
            boolean answerAccepted,
            String newPass
    ) {

        if (pendingPasswordReset == null ||
                !answerAccepted) {

            return LoginResult.error(
                    "No active password reset."
            );
        }

        String validationError =
                validatePassword(newPass);

        if (validationError != null) {

            return LoginResult.error(
                    validationError
            );
        }

        String hashedPass =
                SecurityUtil.hashPassword(
                        newPass
                );

        pendingPasswordReset.setPassword(
                hashedPass
        );

        accountRepository.save();

        return LoginResult.of(
                LoginStatus.PASSWORD_RESET,
                "Password updated successfully."
        );
    }

    private String validatePassword(
            String password
    ) {

        if (!validPasswordLength(password)) {
            return "Invalid password length.";
        }

        if (!validPasswordLower(password)) {
            return "Password must contain a lowercase character.";
        }

        if (!validPasswordUpper(password)) {
            return "Password must contain an uppercase character.";
        }

        if (!validPasswordNumber(password)) {
            return "Password must contain a number.";
        }

        if (!validPasswordSpecific(password)) {
            return "Password must contain a special character.";
        }

        return null;
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
}