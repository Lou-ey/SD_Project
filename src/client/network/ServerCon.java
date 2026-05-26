package client.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ServerCon implements Runnable {
    private int port;
    private String ip;
    private DataOutputStream out;
    private DataInputStream in;

    private boolean connected;

    public ServerCon(int port, String ip, boolean connected) {
        this.port = port;
        this.ip = ip;
        this.connected = connected;
    }

    public void connect() {

        out = new DataOutputStream(System.out);
        in = new DataInputStream(System.in);

        try {
            while (connected) {
                String msg = in.readUTF();
                processServerMessage(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (connected) {
                String msg = in.readUTF();
                processServerMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processServerMessage(String msg) {

    }
}
