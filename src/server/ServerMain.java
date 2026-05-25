package server;

import server.logic.BlackjackTable;
import server.network.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private static int port = 4089;
    public static void main(String[] args) {
        BlackjackTable table = new BlackjackTable();
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, table);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            System.out.println("Error starting the server: " +  e.getMessage());
        }
    }
}
