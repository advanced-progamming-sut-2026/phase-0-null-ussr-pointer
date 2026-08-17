package com.ussr.pvz.network;

import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;

public class NetworkClient {

    private static NetworkClient instance;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private final Gson gson = new Gson();

    private NetworkClient() {
    }

    public static NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }

        return instance;
    }

    public void connect(
            String host,
            int port
    ) throws IOException {

        socket = new Socket(host, port);

        reader = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream()
                )
        );

        writer = new PrintWriter(
                socket.getOutputStream(),
                true
        );
    }

    public NetworkResponse send(
            NetworkRequest request
    ) throws IOException {

        String json =
                gson.toJson(request);

        writer.println(json);

        String responseJson =
                reader.readLine();

        return gson.fromJson(
                responseJson,
                NetworkResponse.class
        );
    }

    public boolean isConnected() {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed();
    }

    public void disconnect() {

        try {

            if (socket != null) {
                socket.close();
            }

        } catch (IOException ignored) {
        }
    }
}