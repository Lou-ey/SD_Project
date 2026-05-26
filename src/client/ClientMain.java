package client;

import client.network.ServerConThread;

public class ClientMain{
    public static void main() {

    String ip = "localhost";
    int porta = 4000;


    ServerConThread serverConThread = new ServerConThread(porta, ip);
    serverConThread.start();

    }
}

