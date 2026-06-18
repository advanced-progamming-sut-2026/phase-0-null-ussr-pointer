package com.ussr.pvz.controller.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ValidationRegex {
    VALID_USERNAME("^[A-Za-z0-9-]+$"),
    VALID_PASSWORD_LENGTH("^.{8,}$"),
    VALID_PASSWORD_LOWER("^.*[a-z].*$"),
    VALID_PASSWORD_UPPER("^.*[A-Z].*$"),
    VALID_PASSWORD_NUMBER("^.*[0-9].*$"),
    VALID_PASSWORD_SPECIFIC_CHARACTER("^.*[!#$%^&*()=+{}|/\\:;'\",<>?].*$"),
    VALID_NICKNAME("^.{3,30}$"),
    VALID_EMAIL("^(?!.*\\.\\.)(?!.*[!#$%^&*()=+{}\\[\\]|\\/\\\\:;'\",<>?])[A-Za-z0-9]" +
            "(?:[A-Za-z0-9._-]*[A-Za-z0-9])?@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"),
    VALID_GENDER("^(Male|Female)$");

    private String regex;

    ValidationRegex(String regex) {
        this.regex = regex;
    }

    public Matcher matchToRegex(String input) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(input);
    }
}
