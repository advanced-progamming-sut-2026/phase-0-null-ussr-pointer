package com.ussr.pvz.server.account;

import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.shared.dto.ChangeEmailRequest;
import com.ussr.pvz.shared.dto.ChangeNicknameRequest;
import com.ussr.pvz.shared.dto.ChangePasswordRequest;
import com.ussr.pvz.shared.dto.ChangeUsernameRequest;
import com.ussr.pvz.shared.util.SecurityUtil;
import com.ussr.pvz.shared.util.ValidationRegex;

public class ProfileService {

    private final AccountRepository accountRepository;
    private final ServerSessionManager sessionManager;

    public ProfileService(
            AccountRepository accountRepository,
            ServerSessionManager sessionManager
    ) {
        this.accountRepository = accountRepository;
        this.sessionManager = sessionManager;
    }


    public String changeUsername(
            String token,
            ChangeUsernameRequest request
    ) {

        Account account =
                getLoggedInAccount(token);

        if (account == null) {
            return "you are not logged in";
        }

        if (!ValidationRegex
                .VALID_USERNAME
                .matchToRegex(request.username())
                .matches()) {

            return "invalid username";
        }

        Account existingAccount =
                accountRepository.findByUsername(
                        request.username()
                );

        if (existingAccount != null &&
                existingAccount != account) {

            return "username already exists";
        }

        account.setName(
                request.username()
        );

        accountRepository.save();

        return "username changed successfully";
    }


    public String changeNickname(
            String token,
            ChangeNicknameRequest request
    ) {

        Account account =
                getLoggedInAccount(token);

        if (account == null) {
            return "you are not logged in";
        }

        if (!ValidationRegex
                .VALID_NICKNAME
                .matchToRegex(request.nickname())
                .matches()) {

            return "invalid nickname length";
        }

        account.setNickname(
                request.nickname()
        );

        accountRepository.save();

        return "nickname changed successfully";
    }


    public String changeEmail(
            String token,
            ChangeEmailRequest request
    ) {

        Account account =
                getLoggedInAccount(token);

        if (account == null) {
            return "you are not logged in";
        }

        if (!ValidationRegex
                .VALID_EMAIL
                .matchToRegex(request.email())
                .matches()) {

            return "invalid email format";
        }

        account.setEmail(
                request.email()
        );

        accountRepository.save();

        return "email changed successfully";
    }


    public String changePassword(
            String token,
            ChangePasswordRequest request
    ) {

        Account account =
                getLoggedInAccount(token);

        if (account == null) {
            return "you are not logged in";
        }

        String oldPasswordHash =
                SecurityUtil.hashPassword(
                        request.oldPassword()
                );

        if (!account.getPassword()
                .equals(oldPasswordHash)) {

            return "old password is incorrect";
        }

        String validationError =
                validatePassword(
                        request.newPassword()
                );

        if (validationError != null) {
            return validationError;
        }

        String newPasswordHash =
                SecurityUtil.hashPassword(
                        request.newPassword()
                );

        account.setPassword(
                newPasswordHash
        );

        accountRepository.save();

        return "password changed successfully";
    }


    public String showInfo(
            String token
    ) {

        Account account =
                getLoggedInAccount(token);

        if (account == null) {
            return "you are not logged in";
        }

        return "username: "
                + account.getName()
                + "\n"
                + "nickname: "
                + account.getNickname()
                + "\n"
                + "email: "
                + account.getEmail()
                + "\n"
                + "gender: "
                + account.getGender()
                .name()
                .toLowerCase()
                + "\n"
                + "coin: "
                + account.getAdventureProgress()
                .getCoin()
                + "\n"
                + "gems: "
                + account.getAdventureProgress()
                .getGem()
                + "\n"
                + "meow points: "
                + account.getScoreRecord()
                .getScore()
                + "\n"
                + "current chapter: "
                + account.getAdventureProgress()
                .getCurrentChapter()
                + "\n"
                + "current level: "
                + account.getAdventureProgress()
                .getCurrentLvl();
    }


    public Account getAccount(
            String token
    ) {

        return getLoggedInAccount(token);
    }


    private Account getLoggedInAccount(
            String token
    ) {

        if (token == null ||
                token.isBlank()) {

            return null;
        }

        return sessionManager
                .getAccount(token);
    }


    private String validatePassword(
            String password
    ) {

        if (!ValidationRegex
                .VALID_PASSWORD_LENGTH
                .matchToRegex(password)
                .matches()) {

            return "invalid password length";
        }

        if (!ValidationRegex
                .VALID_PASSWORD_LOWER
                .matchToRegex(password)
                .matches()) {

            return "password must contain a lowercase character";
        }

        if (!ValidationRegex
                .VALID_PASSWORD_UPPER
                .matchToRegex(password)
                .matches()) {

            return "password must contain an uppercase character";
        }

        if (!ValidationRegex
                .VALID_PASSWORD_NUMBER
                .matchToRegex(password)
                .matches()) {

            return "password must contain a number";
        }

        if (!ValidationRegex
                .VALID_PASSWORD_SPECIFIC_CHARACTER
                .matchToRegex(password)
                .matches()) {

            return "password must contain a specific character";
        }

        return null;
    }
}