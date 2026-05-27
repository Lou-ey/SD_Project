package client.gui;

import client.network.ServerConThread;

import javax.swing.*;
import java.awt.*;

public class TableFrame extends JFrame {
    private ServerConThread serverConThread;
    private String name;

    public TableFrame(ServerConThread serverConThread, String name) {
        this.serverConThread = serverConThread;
        this.name = name;

        initComponents();

        initConnection();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        setTitle("Blackjack - " + name);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        textArea = new javax.swing.JTextArea();
        textArea.setEditable(false);

        btnHit = new javax.swing.JButton();
        btnHit.setText("Hit");

        btnStand = new javax.swing.JButton();
        btnStand.setText("Stand");

        JPanel buttonPanel = new JPanel();

        add(new JScrollPane(textArea), BorderLayout.CENTER);
        buttonPanel.add(btnHit);
        buttonPanel.add(btnStand);
        add(buttonPanel, BorderLayout.SOUTH);

        btnHit.addActionListener(this::btnHitActionPerformed);
        btnStand.addActionListener(this::btnStandActionPerformed);
    }
    // </editor-fold>

    private void initConnection() {
        serverConThread.setTableFrame(this); // Passa referência da TableFrame para o ServerConThread

        Thread thread = new Thread(serverConThread);
        thread.start();

        serverConThread.sendCommand("LOGIN:" + name);
    }

    private void btnHitActionPerformed(java.awt.event.ActionEvent evt) {
        // Enviar comando "HIT" para o servidor
        serverConThread.sendCommand("HIT");
    }

    private void btnStandActionPerformed(java.awt.event.ActionEvent evt) {
        // Enviar comando "STAND" para o servidor
        serverConThread.sendCommand("STAND");
    }

    public void addToTextArea(String text) {
        textArea.append(text + "\n");
    }

    private void drawBlackjack() {

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextArea textArea;
    private JButton btnHit;
    private JButton btnStand;
    // End of variables declaration//GEN-END:variables
}