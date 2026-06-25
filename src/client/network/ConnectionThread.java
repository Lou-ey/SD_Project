package client.network;

import client.gui.TableFrame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * ConnectionThread
 * Thread responsável por manter a conexão com o servidor, ler mensagens e enviar comandos.
 */
public class ConnectionThread extends Thread {
    private int port;
    private String ip;
    private DataOutputStream out;
    private DataInputStream in;
    private Socket socket;

    private String username;

    private boolean connected;

    private TableFrame tableFrame;
    private client.gui.LoginDialog loginDialog; //para controlar a modal 
    
    public void setLoginDialog(client.gui.LoginDialog login) { //passar o objeto da modal login
        this.loginDialog = login;
    }

    public void setTableFrame(TableFrame tableFrame) {
        this.tableFrame = tableFrame;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public ConnectionThread(String ip, int port) {
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
            System.out.println("Conexao perdida: " + e.getMessage());

        } finally {
            disconnect();
        }
    }

    public void processServerMessage(String msg) {
        System.out.println("Server: " + msg);

        String[] parts = msg.split(";");
        String command = "";

        if (parts[0].startsWith("[")) {
            command = parts[1];
        } else {
            command = parts[0];
        }

        switch (command) {

            case "LOGIN_SUCCESS":
                System.out.println(command);
                javax.swing.SwingUtilities.invokeLater(() -> { //meter depois no relatorio
                        loginDialog.loginAceite(this);
                    });
                break;

            case "LOGIN_FAILED":
                System.out.println(command);
                javax.swing.SwingUtilities.invokeLater(() -> {
                        loginDialog.loginRecusado();
                    });
                break;

            default: //se nao for logins deve ser para desenhar ou sair
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (tableFrame != null){
                        tableFrame.processServerMessage(parts, msg);
                    }
                });
                break; //depois mandar as mensagens "cruas" para a mesa e la fazer a filtragem la para atualizar

        }
    }

    public void sendCommand(String command) {
        try{
           out.writeUTF(command);
           out.flush();

        }catch (IOException e){
            System.out.println(e);
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
