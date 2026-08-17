package com.ussr.pvz.shared.network;

import com.google.gson.JsonObject;

public class NetworkRequest {

    private RequestType type;
    private String token;
    private JsonObject data;

    public NetworkRequest(
            RequestType type,
            String token,
            JsonObject data
    ) {
        this.type = type;
        this.token = token;
        this.data = data;
    }

    public NetworkRequest(
            RequestType type,
            JsonObject data
    ) {
        this(type, null, data);
    }

    public RequestType getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public JsonObject getData() {
        return data;
    }
}