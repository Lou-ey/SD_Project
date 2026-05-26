package client.network;

import client.gui.Table;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerCon implements Runnable {
    private int port;
    private String ip;
    private DataOutputStream out;
    private DataInputStream in;
    private Socket socket;

    private boolean connected;

    private Table table;

    public ServerCon(int port, String ip, boolean connected) {
        this.port = port;
        this.ip = ip;
        this.connected = connected;
    }

    public boolean connect() {
        try {
            this.socket = new Socket(ip, port);
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            this.connected = true;
            return true;
        } catch (Exception e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void run() {
        try {
            while (connected) {
                String serverMessage = in.readUTF();
                processServerMessage(serverMessage);
            }
        } catch (Exception e) {
            System.out.println("Connection lost: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void processServerMessage(String msg) {
        System.out.println("Server: " + msg);

        String[] parts = msg.split(":");
        String command = parts[0];

        switch (command) {
            case "PLAYER_CARD":
                break;
            case "DEALER_CARD":
                break;
            case "CHIPS":
                break;
            default:
                break;
        }
    }

    public void sendCommand(String command) {
        if (out != null && connected) {
            try {
                out.writeUTF(command);
                out.flush();
            } catch (IOException e) {
                System.out.println("Failed to send command: " + e.getMessage());
            }
        }
    }

    public void disconnect() {
        this.connected = false;
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}
