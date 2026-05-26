package client.network;

import client.gui.TableFrame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ServerConThread extends Thread {
    private int port;
    private String ip;
    private DataOutputStream out;
    private DataInputStream in;
    private Socket socket;

    private boolean connected;

    private TableFrame tableFrame;

    public ServerConThread(int port, String ip) {
        this.port = port;
        this.ip = ip;
        this.connected = false;
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

        }
    }

    public void processServerMessage(String msg) {
        System.out.println("Server: " + msg);

        String[] parts = msg.split(":");
        String command = parts[0];

        switch (command) {
            case "PLAYER_CARD":
                System.out.println(command);
                break;
            case "DEALER_CARD":
                System.out.println(command);

                break;
            case "CHIPS":
                System.out.println(command);

                break;
            default:
                break;
        }
    }

    public void sendCommand(String command) {

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
