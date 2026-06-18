package com.ussr.pvz.controller;

import com.ussr.pvz.controller.command.RegisterCommand;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.dto.PickQuestionRequest;
import com.ussr.pvz.model.dto.RegisterRequest;
import com.ussr.pvz.service.RegisterService;

import java.util.regex.Matcher;

public class RegisterController {
    RegisterService service = new RegisterService();

    public RegisterController() {
    }

    public String handleCommand(String command) {
        for (RegisterCommand cmd : RegisterCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case REGISTER -> handleRegister(matcher);
                    case PICK_QUESTION -> handlePickQuestion(matcher);
                    case SHOW_CURRENT_MENU -> handleShowMenu();
                };
            }
        }
        return "";
    }

    private String handleRegister(Matcher matcher) {
        RegisterRequest request = new RegisterRequest(
                matcher.group("username"),
                matcher.group("password"),
                matcher.group("passwordConfirm"),
                matcher.group("nickname"),
                matcher.group("email"),
                matcher.group("gender")
        );

        return service.register(request);
    }

    private String handlePickQuestion(Matcher matcher) {
        PickQuestionRequest request = new PickQuestionRequest(
                matcher.group("questionNumber"),
                matcher.group("answer"),
                matcher.group("answerConfirm")
        );


        return service.pickQuestion(request);
    }

    private String handleShowMenu() {
        return "current menu:" + App.getMenuState().getName();
    }
}