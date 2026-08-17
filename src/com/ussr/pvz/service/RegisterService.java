package com.ussr.pvz.service;

import com.google.gson.JsonObject;
import com.ussr.pvz.network.NetworkClient;
import com.ussr.pvz.shared.dto.PickQuestionRequest;
import com.ussr.pvz.shared.dto.RegisterRequest;
import com.ussr.pvz.shared.dto.RegistrationResult;
import com.ussr.pvz.shared.dto.enums.RegistrationStatus;
import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;
import com.ussr.pvz.shared.network.RequestType;

public class RegisterService {

    private final NetworkClient networkClient;

    public RegisterService() {

        this.networkClient =
                NetworkClient.getInstance();
    }


    public RegistrationResult register(
            RegisterRequest registerRequest
    ) {

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "username",
                registerRequest.username()
        );

        data.addProperty(
                "password",
                registerRequest.password()
        );

        data.addProperty(
                "passwordConfirm",
                registerRequest.passwordConfirm()
        );

        data.addProperty(
                "nickname",
                registerRequest.nickname()
        );

        data.addProperty(
                "email",
                registerRequest.email()
        );

        data.addProperty(
                "gender",
                registerRequest.gender()
        );

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.REGISTER,
                        data
                );

        NetworkResponse response =
                send(request);

        return convertToRegistrationResult(
                response
        );
    }


    public RegistrationResult pickQuestion(
            PickQuestionRequest pickQuestionRequest
    ) {

        JsonObject data =
                new JsonObject();

        data.addProperty(
                "questionNumber",
                pickQuestionRequest.questionNumber()
        );

        data.addProperty(
                "answer",
                pickQuestionRequest.answer()
        );

        data.addProperty(
                "answerConfirm",
                pickQuestionRequest.answerConfirm()
        );

        NetworkRequest request =
                new NetworkRequest(
                        RequestType.COMPLETE_REGISTRATION,
                        data
                );

        NetworkResponse response =
                send(request);

        return convertToRegistrationResult(
                response
        );
    }


    private NetworkResponse send(
            NetworkRequest request
    ) {

        try {

            return networkClient.send(
                    request
            );

        } catch (Exception e) {

            System.err.println(
                    "Network error: "
                            + e.getMessage()
            );

            return null;
        }
    }


    private RegistrationResult convertToRegistrationResult(
            NetworkResponse response
    ) {

        if (response == null) {

            return RegistrationResult.error(
                    "Could not connect to server."
            );
        }

        if (response.getData() == null ||
                !response.getData()
                        .has("status")) {

            return RegistrationResult.error(
                    response.getMessage()
            );
        }

        try {

            RegistrationStatus status =
                    RegistrationStatus.valueOf(
                            response
                                    .getData()
                                    .get("status")
                                    .getAsString()
                    );

            return new RegistrationResult(
                    status,
                    response.getMessage()
            );

        } catch (IllegalArgumentException e) {

            return RegistrationResult.error(
                    response.getMessage()
            );
        }
    }
}