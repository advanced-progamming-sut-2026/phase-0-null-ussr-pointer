package com.ussr.pvz.service;

import com.ussr.pvz.controller.command.ValidationRegex;
import com.ussr.pvz.model.dto.RegisterRequest;

public class RegisterService {
    public String register(RegisterRequest request) {
        if(!validUsername(request.username()))
            return "invalid username";
        else if(!validPasswordLength(request.password()))
            return "invalid password length";
        else if(!validPasswordLower(request.password()))
            return "password must contain a lowercase character";
        else if(!validPasswordUpper(request.password()))
            return "password must contain an uppercase character";
        else if(!validPasswordNumber(request.password()))
            return "password must contain a number";
        else if(!validPasswordSpecific(request.password()))
            return "password must contain a specific character";
        else if(!request.password().equals(request.passwordConfirm()))
            return "password confirm does not match to the password";
        else if(!validEmail(request.email()))
            return "invalid email format";
        else if(!validNickname(request.nickname()))
            return "invalid nickname length";
        else if(!validGender(request.gender()))
            return "invalid gender";


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
