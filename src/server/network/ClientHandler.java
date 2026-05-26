package server.network;

import server.logic.BlackjackTable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;

/**
 * Thread
 * Cada thread é um cliente.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private BlackjackTable table;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public ClientHandler(Socket socket, BlackjackTable table) {
        this.socket = socket;
        this.table = table;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            while(true) {
                String message = in.readUTF();
                System.out.println("Received: " + message + " from client: " + username);

                // o formato da mensagem é "COMMAND:DATA", onde COMMAND é a ação e DATA é o conteúdo
                String[] parts = message.split(":", 2);
                String command = parts[0];

                switch (command) {
                    case "LOGIN":
                        if(parts.length < 2) {
                            sendMessage("Error: Nome do utilizador em falta!");
                            break;
                        }
                        String name = parts[1];
                        boolean success = table.addPlayer(name, this);
                        if(success) {
                            this.username = name;
                            sendMessage("LOGIN_SUCCESS:Bem-vindo, " + name + "!");
                            break;
                        } else {
                            sendMessage("LOGIN_FAILED:Nome de utilizador já existe!");
                        }
                        break;
                    case "HIT":
                        System.out.println("Player " + username + " hit!");
                        table.requestHit(username);
                        break;
                    case "STAND":
                        System.out.println("Player " + username + " stands!");
                        table.requestStand(username);
                        break;
                    case "LOGOUT":
                        return;
                    default:
                        break;
                }
            }
        } catch(IOException e) {
            System.out.println("Error in client handler with client: " + username);
        } finally {
            if (username != null) {
                // se saiu while desconecta o cliente
                // tratar a saida do jogador passar a observador ou remover do mesa
                table.removePlayer(username);
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket for client: " + username);
            }
        }
    }

    public void sendMessage(String msg) {
        try {
            if (out != null) {
                out.writeUTF(msg);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Error sending message to client: " + username);
        }
    }

    public String getUsername() {
        return username;
    }
}
