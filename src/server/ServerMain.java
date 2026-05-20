package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private static int port = 4089;
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
            }

        } catch (IOException e) {
            System.out.println("Error starting the server: " +  e.getMessage());
        }
    }
}
