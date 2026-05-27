package client.gui;

import client.network.ServerConThread;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {

    public LoginDialog() {
        initComponents();
        setLocationRelativeTo(null);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        setTitle("Login");
        setSize(300,200);
        setModal(true);
        setResizable(false);

        ipLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        ipTextField = new javax.swing.JTextField();
        portTextField = new javax.swing.JTextField();
        nameTextField = new javax.swing.JTextField();
        btnLogin = new javax.swing.JButton();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ipLabel.setText("IP:");
        ipTextField.setText("localhost");

        portLabel.setText("Port:");
        portTextField.setText("4000");

        nameLabel.setText("Name:");

        btnLogin.setText("Login");
        btnLogin.addActionListener(this::btnLoginActionPerformed);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(ipLabel);
        panel.add(ipTextField);
        panel.add(portLabel);
        panel.add(portTextField);
        panel.add(nameLabel);
        panel.add(nameTextField);
        panel.add(btnLogin);

        add(panel);
    }
    // </editor-fold>

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        String ip = ipTextField.getText().trim();
        String portStr = portTextField.getText().trim();
        String name = nameTextField.getText().trim();

        if (ip.isEmpty() || portStr.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            ServerConThread serverConThread = new ServerConThread(ip, port);

            if (serverConThread.connect()) {
                TableFrame tableFrame = new TableFrame(serverConThread, name); // cria a janela da mesa passando a conexão e o nome do jogador para nao perder a referencia

                this.dispose(); // fecha o diálogo de login

                tableFrame.setVisible(true); // mostra a janela da mesa
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao conectar ao servidor");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Porto inválido");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ipLabel;
    private javax.swing.JLabel portLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField ipTextField;
    private javax.swing.JTextField portTextField;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton btnLogin;
    // End of variables declaration//GEN-END:variables
}
