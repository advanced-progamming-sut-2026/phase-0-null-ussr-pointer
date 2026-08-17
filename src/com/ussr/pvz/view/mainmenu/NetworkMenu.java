package com.ussr.pvz.view.mainmenu;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.network.NetworkClient;
import com.ussr.pvz.network.NetworkRequest;
import com.ussr.pvz.network.NetworkResponse;
import com.ussr.pvz.network.RequestType;
import com.ussr.pvz.view.AppMenu;

import java.io.IOException;
import java.util.Scanner;

public class NetworkMenu implements AppMenu {

    @Override
    public void run(Scanner scanner) {

        NetworkClient client =
                NetworkClient.getInstance();

        try {

            if (!client.isConnected()) {
                client.connect(
                        "127.0.0.1",
                        8080
                );
            }

            NetworkRequest request =
                    new NetworkRequest(
                            RequestType.PING,
                            null
                    );

            NetworkResponse response =
                    client.send(request);

            System.out.println(
                    "Server response: "
                            + response.getMessage()
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not connect to server: "
                            + e.getMessage()
            );
        }

        App.setMenuState(
                MenuState.MAIN
        );
    }
}