package com.ussr.pvz.shared.network;

import com.google.gson.JsonObject;

public class NetworkResponse {

    private boolean success;
    private String message;
    private JsonObject data;

    public NetworkResponse(
            boolean success,
            String message,
            JsonObject data
    ) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static NetworkResponse success(
            String message
    ) {
        return new NetworkResponse(
                true,
                message,
                null
        );
    }

    public static NetworkResponse success(
            String message,
            JsonObject data
    ) {
        return new NetworkResponse(
                true,
                message,
                data
        );
    }

    public static NetworkResponse error(
            String message
    ) {
        return new NetworkResponse(
                false,
                message,
                null
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public JsonObject getData() {
        return data;
    }
}