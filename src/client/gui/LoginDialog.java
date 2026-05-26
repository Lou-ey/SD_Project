package client.gui;

import client.network.ServerConThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginDialog extends JDialog {
    private JTextField ipTextField;
    private JTextField portTextField;
    private JTextField nameTextField;
    private JButton btnLogin;

    public LoginDialog() {
        setTitle("Login");
        setSize(300,200);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("IP:"));
        ipTextField = new JTextField("localhost");
        panel.add(ipTextField);

        panel.add(new JLabel("Porto:"));
        portTextField = new JTextField("4000");
        panel.add(portTextField);

        panel.add(new JLabel("Nome:"));
        nameTextField = new JTextField();
        panel.add(nameTextField);

        panel.add(new JLabel("Login"));
        btnLogin = new JButton("Login");
        panel.add(btnLogin);

        add(panel);

        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
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
            JOptionPane.showMessageDialog(this, "Porto inválida");
        }
    }
}
