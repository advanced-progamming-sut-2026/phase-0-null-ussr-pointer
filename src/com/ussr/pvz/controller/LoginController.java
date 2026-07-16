package com.ussr.pvz.controller;

import com.ussr.pvz.controller.command.LoginCommand;
import com.ussr.pvz.model.dto.AnswerRequest;
import com.ussr.pvz.model.dto.ForgetPasswordRequest;
import com.ussr.pvz.model.dto.LoginRequest;
import com.ussr.pvz.service.LoginService;

import java.util.regex.Matcher;

public class LoginController {
    private final LoginService loginService = new LoginService();

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
                    case MENU_CHANGE_REGISTER:
                        return handle_change_menu();
                    default:
                        return handleResetPassword(command);
                }
            }
        }
        return "";
    }

    private String handle_change_menu() {
        return loginService.handleSwitchRegister();
    }

    private String handleLogin(Matcher matcher) {
        boolean stayLoggedIn = matcher.group("stayLoggedIn") != null;

        LoginRequest request = new LoginRequest(
                matcher.group("username"),
                matcher.group("password"),
                stayLoggedIn
        );


        return loginService.login(request);
    }

    private String handleForgetPassword(Matcher matcher) {
        ForgetPasswordRequest request = new ForgetPasswordRequest(
                matcher.group("username"),
                matcher.group("email")
        );


        return loginService.forgetPassword(request);
    }

    private String handleAnswer(Matcher matcher) {
        AnswerRequest request = new AnswerRequest(
                matcher.group("answer")
        );
        return loginService.answer(request);
    }

    private String handleResetPassword(String newPass) {
        return loginService.resetPassword(newPass);
    }
}