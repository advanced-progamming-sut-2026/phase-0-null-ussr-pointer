package com.ussr.pvz.controller;

import com.ussr.pvz.controller.command.RegisterCommand;
import com.ussr.pvz.model.dto.RegisterRequest;
import com.ussr.pvz.model.dto.PickQuestionRequest;

import java.util.regex.Matcher;

public class RegisterController {

    public RegisterController() {
    }

    public String handleCommand(String command) {
        for (RegisterCommand cmd : RegisterCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                switch (cmd) {
                    case REGISTER:
                        return handleRegister(matcher);
                    case PICK_QUESTION:
                        return handlePickQuestion(matcher);
                    default:
                        return "";
                }
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

        // TODO: call registerService.register(request) and return its message

        return "";
    }

    private String handlePickQuestion(Matcher matcher) {
        PickQuestionRequest request = new PickQuestionRequest(
                matcher.group("questionNumber"),
                matcher.group("answer"),
                matcher.group("answerConfirm")
        );

        // TODO: call registerService.pickQuestion(request) and return its message

        return "";
    }
}