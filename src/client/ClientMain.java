package client;

import client.gui.LoginDialog;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientMain{
    public static void main(String[] args) {
        LoginDialog loginDialog = new LoginDialog();

        loginDialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        loginDialog.setVisible(true);
    }
}