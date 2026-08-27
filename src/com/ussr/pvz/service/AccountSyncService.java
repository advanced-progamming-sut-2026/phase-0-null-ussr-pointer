package com.ussr.pvz.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.util.SessionManager;
import com.ussr.pvz.network.NetworkClient;
import com.ussr.pvz.shared.account.AccountState;
import com.ussr.pvz.shared.network.NetworkRequest;
import com.ussr.pvz.shared.network.NetworkResponse;
import com.ussr.pvz.shared.network.RequestType;

public final class AccountSyncService {

    private static final Gson gson = new Gson();

    private AccountSyncService() {
    }

    public static void sync() {
        if (App.getAccount() == null) return;

        String token = SessionManager.getToken();
        if (token == null || token.isBlank()) return;

        try {
            AccountState state = App.getAccount().toState();
            JsonObject data = gson.toJsonTree(state).getAsJsonObject();

            NetworkRequest request = new NetworkRequest(RequestType.SYNC_ACCOUNT, token, data);
            NetworkResponse response = NetworkClient.getInstance().send(request);

            if (response == null || !response.isSuccess()) {
                System.err.println("Account sync failed: "
                        + (response != null ? response.getMessage() : "no response"));
            }
        } catch (Exception e) {
            System.err.println("Account sync error: " + e.getMessage());
        }
    }
}