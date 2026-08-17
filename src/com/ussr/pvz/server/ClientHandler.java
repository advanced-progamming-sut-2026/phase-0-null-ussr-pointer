package com.ussr.pvz.server;

import com.google.gson.Gson;
import com.ussr.pvz.network.NetworkRequest;
import com.ussr.pvz.network.NetworkResponse;

import java.io.*;
import java.net.Socket;

public class ClientHandler
        implements Runnable {

    private final Socket socket;

    private BufferedReader reader;
    private PrintWriter writer;

    private final Gson gson =
            new Gson();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {

            reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            writer = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            String line;

            while ((line = reader.readLine())
                    != null) {

                NetworkRequest request =
                        gson.fromJson(
                                line,
                                NetworkRequest.class
                        );

                NetworkResponse response =
                        handleRequest(request);

                writer.println(
                        gson.toJson(response)
                );
            }

        } catch (IOException e) {

            System.out.println(
                    "Client disconnected."
            );

        } finally {

            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private NetworkResponse handleRequest(
            NetworkRequest request
    ) {

        return switch (request.getType()) {

            case PING ->
                    NetworkResponse.success(
                            "PONG"
                    );

            default ->
                    NetworkResponse.error(
                            "Request not implemented."
                    );
        };
    }
}