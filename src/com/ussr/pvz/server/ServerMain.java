package com.ussr.pvz.server;

public class ServerMain {

    public static void main(String[] args) {

        PvZServer server =
                new PvZServer(8080);

        server.start();
    }
}