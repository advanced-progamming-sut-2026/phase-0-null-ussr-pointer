package com.ussr.pvz.controller;

import com.ussr.pvz.controller.command.LoginCommand;
import com.ussr.pvz.model.dto.LoginRequest;
import com.ussr.pvz.model.dto.ForgetPasswordRequest;
import com.ussr.pvz.model.dto.AnswerRequest;

import java.util.regex.Matcher;

public class LoginController {

    public LoginController() {
    }

    public String handleCommand(String command) {
        for (LoginCommand cmd : LoginCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                switch (cmd) {
                    case LOGIN:
                        return handleLogin(matcher);
                    case FORGET_PASSWORD:
                        return handleForgetPassword(matcher);
                    case ANSWER:
                        return handleAnswer(matcher);
                    default:
                        return "";
                }
            }
        }
        return "";
    }

    private String handleLogin(Matcher matcher) {
        boolean stayLoggedIn = matcher.group("stayLoggedIn") != null;

        LoginRequest request = new LoginRequest(
                matcher.group("username"),
                matcher.group("password"),
                stayLoggedIn
        );

        // TODO: call loginService.login(request) and return its message

        return "";
    }

    private String handleForgetPassword(Matcher matcher) {
        ForgetPasswordRequest request = new ForgetPasswordRequest(
                matcher.group("username"),
                matcher.group("email")
        );

        // TODO: call loginService.forgetPassword(request) and return its message

        return "";
    }

    private String handleAnswer(Matcher matcher) {
        AnswerRequest request = new AnswerRequest(
                matcher.group("answer")
        );

        // TODO: call loginService.answer(request) and return its message

        return "";
    }
}