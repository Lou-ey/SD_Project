package client.gui;

import client.network.ServerConThread;

import javax.swing.*;
import java.awt.*;

public class TableFrame extends JFrame {
    private ServerConThread serverConThread;
    private String name;

    private JTextArea textArea;
    private JButton btnHit;
    private JButton btnStand;

    public TableFrame(ServerConThread serverConThread, String name) {
        this.serverConThread = serverConThread;
        this.name = name;

        setTitle("Blackjack - " + name);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        initConnection();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        btnHit = new JButton("Hit");
        btnStand = new JButton("Stand");

        buttonPanel.add(btnHit);
        buttonPanel.add(btnStand);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initConnection() {
        serverConThread.setTableFrame(this);

        Thread thread = new Thread(serverConThread);
        thread.start();

        serverConThread.sendCommand("LOGIN:" + name);
    }

    private void handleHit() {
        // Enviar comando "HIT" para o servidor
        serverConThread.sendCommand("HIT");
    }

    private void handleStand() {
        // Enviar comando "STAND" para o servidor
        serverConThread.sendCommand("STAND");
    }

    public void addToTextArea(String text) {
        textArea.append(text + "\n");
    }
}