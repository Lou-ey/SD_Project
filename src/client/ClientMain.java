package client;

import client.gui.TableFrame;

public class ClientMain {

    public static void main(String[] args) {
        // Invoca a interface gráfica de forma segura atraves do invokeLater
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TableFrame();
            }
        });
    }
}