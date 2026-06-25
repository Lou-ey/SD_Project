/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package client.gui;

import client.model.CardLabel;
import client.network.ConnectionThread;

/**
 *
 * @author G513
 */
public class TableFrame extends javax.swing.JFrame {

    private ConnectionThread connectionThread;

    private java.util.Map<String, Integer> contadorDeCartas = new java.util.HashMap<>();

    private int xDealerCard = 360;
    private CardLabel cartaEscondidaDealer = null; //para depois desenhar a parte da frente

    private String listaEspetadores = "Ninguém";

    private boolean estouSentado = false;

    /**
     * Creates new form TableFramee
     */
    public TableFrame() {
        initComponents();
        //this.jPanel1.setLayout(null);
        this.setLocationRelativeTo(null);//centrar

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        
        this.hitButton.setEnabled(false);
        this.standButton.setEnabled(false);

        this.jLabelPlayer0.setVisible(false);
        this.jLabelPlayer1.setVisible(false);
        this.jLabelPlayer2.setVisible(false);
        this.jLabelDealer.setVisible(false);
        this.jLabelPts0.setVisible(false);
        this.jLabelPts1.setVisible(false);
        this.jLabelPts2.setVisible(false);
        if (this.jLabelFichas != null) {
            this.jLabelFichas.setVisible(false);
        }

        // definir DO_NOTHING_ON_CLOSE para evitar que a janela feche imediatamente e dar tempo para enviar o comando de logout
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                fecharJanelaGracefully();
            }
        });

        conectarAoServidor();
    }

    public void setConnectionThread(ConnectionThread thread) {
        this.connectionThread = thread;
    }

    private void fecharJanelaGracefully() {
        if (this.connectionThread != null) {
           
            this.connectionThread.sendCommand("LOGOUT:sair");
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {}
            
            
            this.connectionThread.disconnect();
        }
        
        System.exit(0);
    }

    private void conectarAoServidor() {
        //abrir o dialog do login
        LoginDialog loginFrame = new LoginDialog(this, true);
        loginFrame.setVisible(true);
        
        this.connectionThread = loginFrame.getServerConThread();

        if (this.connectionThread != null) {
            //user conseguiu dar login
            loginFrame.dispose();
            
            this.setTitle("Blackjack - Jogador: " +  this.connectionThread.getUsername());
            
            this.setVisible(true); //mostrar a mesa

            //passar a referencia da janela para a thread
            this.connectionThread.setTableFrame(this);
        } else {
            System.exit(0);
        }
    }

    public void drawDealerCard(String nomeCarta, String virada, String total){

        if(virada.equals("face down reveal")){
            if (this.cartaEscondidaDealer != null) {
                this.cartaEscondidaDealer.setCardImage(nomeCarta);
                this.cartaEscondidaDealer.setCardCovered(false);
                this.jLabelDealer.setText("Dealer: " + total + " pts");
                this.jPanel1.repaint();
            }
            return;
        }

        CardLabel novaCartaDealer = new CardLabel();

        if (virada.equals("face down")){
            novaCartaDealer.setCardImage("bv");
            novaCartaDealer.setCardCovered(false);
            this.cartaEscondidaDealer = novaCartaDealer;

            this.jLabelDealer.setVisible(true);

        } else if(virada.equals("face up")){
            this.jLabelDealer.setText("Dealer: " + total + " pts");
            novaCartaDealer.setCardImage(nomeCarta);
            novaCartaDealer.setCardCovered(false);
        }

        novaCartaDealer.setLocation(this.xDealerCard, 45);
        
        this.jPanel1.add(novaCartaDealer, 0);
        
        xDealerCard += 20;

        this.jPanel1.revalidate();
        this.jPanel1.repaint();
    }
    
    public void drawPlayersCard(String playerName, String nomeCarta, String total, String posicaoJogador) {
        
        int baseX = 0;
        int baseY = 460;
        int numeroDeCartas = contadorDeCartas.getOrDefault(playerName, 0);

        switch (posicaoJogador) {
            case "0":
                baseX = 360;
                this.jLabelPlayer0.setVisible(true);
                this.jLabelPts0.setVisible(true);
                this.jLabelPlayer0.setText(playerName);
                this.jLabelPts0.setText(total + "pts");
                break;
            case "1":
                baseX = 80;
                baseY = 440;
                this.jLabelPlayer1.setVisible(true);
                this.jLabelPts1.setVisible(true);
                this.jLabelPlayer1.setText(playerName);
                this.jLabelPts1.setText(total + "pts");
                break;
            case "2":
                baseX = 655;
                baseY = 440;
                this.jLabelPlayer2.setVisible(true);
                this.jLabelPts2.setVisible(true);
                this.jLabelPlayer2.setText(playerName);
                this.jLabelPts2.setText(total + "pts");
                break;
        }

        if (playerName.equals(this.connectionThread.getUsername())) {

            int alturaDaCarta = 96;
            int espaco = 20; // Espaço entre as cartas e os botões
            int botoesY = baseY + alturaDaCarta + espaco + 70;

            // botão HIT alinhado com a primeira carta
            this.hitButton.setLocation(baseX - 47, botoesY - 15);

            // botão STAND ao lado do HIT
            this.standButton.setLocation(baseX + 60, botoesY - 15);

            // mover a label das Fichas para baixo dos botões!
            if (this.jLabelFichas != null) {
                this.jLabelFichas.setLocation(baseX + 120, botoesY - 40);
            }
        }

        int xFinal = baseX + (numeroDeCartas * 20);

        CardLabel novaCarta = new CardLabel();
        novaCarta.setCardImage(nomeCarta);

        if (numeroDeCartas == 0) {
            novaCarta.setCardCovered(true);
        } else {
            novaCarta.setCardCovered(false);
        }

        novaCarta.setLocation(xFinal, baseY); 

        this.jPanel1.add(novaCarta, 0);
        this.jPanel1.revalidate();
        this.jPanel1.repaint();
        
        contadorDeCartas.put(playerName, numeroDeCartas + 1);
    }

    // Mostra o jogador na cadeira assim que ele entra, antes de ter cartas
    private void mostrarNomeNaMesa(String playerName, String posicaoJogador) {
        switch (posicaoJogador) {
            case "0":
                this.jLabelPlayer0.setVisible(true);
                this.jLabelPlayer0.setText(playerName + " (A espera...)");
                break;
            case "1":
                this.jLabelPlayer1.setVisible(true);
                this.jLabelPlayer1.setText(playerName + " (A espera...)");
                break;
            case "2":
                this.jLabelPlayer2.setVisible(true);
                this.jLabelPlayer2.setText(playerName + " (A espera...)");
                break;
        }
    }

    public void limparMesa() {
        //lista para armazenar tudo o que tem no painel
        java.awt.Component[] componentes = this.jPanel1.getComponents();

        for (java.awt.Component comp : componentes) {
            //remover so as cartas
            if (comp instanceof CardLabel) {
                this.jPanel1.remove(comp);
            }
        }

        this.cartaEscondidaDealer = null;
        xDealerCard = 270; //repor a pos das cartas do dealer

        this.jLabelPlayer0.setVisible(false);
        this.jLabelPlayer1.setVisible(false);
        this.jLabelPlayer2.setVisible(false);
        this.jLabelDealer.setVisible(false);
        this.jLabelPts0.setVisible(false);
        this.jLabelPts1.setVisible(false);
        this.jLabelPts2.setVisible(false);

        this.jPanel1.revalidate();
        this.jPanel1.repaint();
        this.contadorDeCartas.clear();
    }

    public void writeInTextBox(String msg){
        this.jTextArea1.append(msg + "\n");
    }

    public void processServerMessage(String[] parts, String msg) {

        String command = parts[1];

        String meuNome = connectionThread.getUsername();

        String hora = parts[0];

        switch (command) {
            case "BEGIN":
                limparMesa();
                this.xDealerCard = 360;
                this.lblStatusCentral.setText("Nova ronda a começar!");
                writeInTextBox(hora + " -----------------------------------");
                writeInTextBox(hora + " Uma nova ronda começou!");

                if (estouSentado) {
                    this.jButtonSair.setEnabled(false);
                    this.jButtonModoEsp.setEnabled(false);
                } else {
                    this.jButtonSair.setEnabled(true);
                    this.jButtonModoEsp.setEnabled(false);
                }
                break;

            case "ROUND_START": // ROUND_START;nome,fichas
                if(parts[2].equals(connectionThread.getUsername())) {
                    this.jLabelFichas.setVisible(true);
                    this.jLabelFichas.setText("Fichas: " + parts[3]);
                }
                writeInTextBox(hora + " " + parts[2] + "apostou 2 fichas. (Restantes: " +  parts[3] + ")");
                break;

            case "PLAYER_CARD":
                drawPlayersCard(parts[2], parts[3], parts[4], parts[5]);
                break;

            case "DEALER_CARD":
                drawDealerCard(parts[2], parts[3], parts[4]);
                break;

            case "INFO":
                if (parts.length <= 2) {
                    break;
                }
                String informacao = parts[2];

                if (informacao.equals("WAITING_PLAYERS") || informacao.contains("aguardar")) {
                    this.lblStatusCentral.setText("A ronda vai começar em 10 segundos...");
                    writeInTextBox(hora + " A aguardar jogadores. O jogo começa em 10 segundos.");

                    this.jButtonSair.setEnabled(true);
                    if (estouSentado) {
                        this.jButtonModoEsp.setEnabled(true);
                    }

                } else if (informacao.equals("CARDS_DEALT") || informacao.contains("distribuídas")) {
                    this.lblStatusCentral.setText("Cartas distribuídas!");
                    writeInTextBox(hora + " As cartas foram distribuídas.");

                } else {
                    this.lblStatusCentral.setText(informacao);
                    writeInTextBox(hora + " " + informacao);
                }
                break;
            case "TURN": //TURN;nome
                String jogadorAJogar = parts[2];

                if (jogadorAJogar.equals(meuNome)) {
                    this.hitButton.setEnabled(true);
                    this.standButton.setEnabled(true);
                    this.lblStatusCentral.setText("É a tua vez de jogar, " + meuNome + "!");
                } else {
                    this.hitButton.setEnabled(false);
                    this.standButton.setEnabled(false);
                    this.lblStatusCentral.setText("A espera da jogada de " + jogadorAJogar);
                }
                writeInTextBox(hora + " É vez de " + jogadorAJogar + " jogar.");
                break;

            case "STAND": // STAND;nome;pts
                this.standButton.setEnabled(false);
                this.hitButton.setEnabled(false);

                if (parts[2].equals(meuNome)) {
                    this.lblStatusCentral.setText("Deste STAND com " + parts[3] + "pontos");
                } else {
                    this.lblStatusCentral.setText(parts[2] + " fez STAND.");
                }

                writeInTextBox(hora + parts[2] + " fez STAND com " + parts[3] + " pontos.");
                break;

            case "BUST": //BUST;nome;pts
                this.hitButton.setEnabled(false);
                this.standButton.setEnabled(false);

                if (parts[2].equals(meuNome)) {
                    this.lblStatusCentral.setText("Rebentaste! com " + parts[3] + "pontos");
                } else {
                    this.lblStatusCentral.setText(parts[2] + " rebentou!");
                }

                writeInTextBox(hora + " " + parts[2] + " rebento com " + parts[3] + " pontos.");
                break;

            case "DEALER_TURN": //DEALER_TURN
                this.lblStatusCentral.setText("Vez do Dealer jogar.");
                writeInTextBox(hora + " É vez do Dealer jogar.");
                break;

            case "DEALER_BUST": // DEALER_BUST;ptsDealer
                this.lblStatusCentral.setText("O Dealer rebentou!");
                writeInTextBox(hora + " O Dealer rebentou com " + parts[2] + " pontos.");
                break;

            case "DEALER_STAND": //DEALER_STAND;ptsDealer
                this.lblStatusCentral.setText("O Dealer fez STAND com " + parts[2] + " pontos.");
                writeInTextBox(hora + " O Dealer fez STAND com " + parts[2] + " pontos.");
                break;

            case "WARNING":
                if (parts[2].equals("TIMEOUT")) {
                    if (parts[3].equals(meuNome)) {
                        this.lblStatusCentral.setText("O tempo para jogar acabou!");
                    }
                    writeInTextBox(hora + " " + parts[3] + " esgotou o tempo para jogar! (STAND Automatico)");
                }
                break;

            case "RESULTS":
                this.lblStatusCentral.setText("Fim da ronda.");
                writeInTextBox(hora + parts[2]);

                this.jButtonSair.setEnabled(true);

                if (estouSentado) {
                    this.jButtonModoEsp.setEnabled(true);
                }
                break;

            case "RESULT": // RESULT;tipoVitoria;nome;pontosJog;pontosDealer;fichas
                String tipoVitoria = parts[2];
                String nome = parts[3];
                String pontosJog = parts[4];
                String pontosDealer = parts[5];
                String fichas = parts[6];

                this.jButtonModoEsp.setEnabled(true);
                this.jButtonSair.setEnabled(true);

                if (nome.equals(meuNome)) {
                    this.jLabelFichas.setText("Fichas: " + fichas);

                    if (tipoVitoria.startsWith("WIN")) {
                        this.lblStatusCentral.setText("Ganhaste a ronda!");
                    }
                    else if (tipoVitoria.startsWith("TIE")) {
                        this.lblStatusCentral.setText("Empataste.");
                    } else {
                        this.lblStatusCentral.setText("Perdeste a ronda.");
                    }
                }

                String msgResultado = "";
                switch (tipoVitoria) {
                    case "LOSE_BUST":
                        msgResultado = nome + " rebentou e perdeu.";
                        break;
                    case "WIN_BLACKJACK":
                        msgResultado = nome + " fez BLACKJACK e ganhou 5 fichas!";
                        break;
                    case "TIE_BLACKJACK":
                        msgResultado = nome + " empatou (Ambos Blackjack). Recebeu 2 fichas.";
                        break;
                    case "WIN_DEALER_BUST":
                        msgResultado = nome + " ganhou (Dealer Bust). Ganhou 4 fichas.";
                        break;
                    case "TIE_NORMAL":
                        msgResultado = nome + " empatou (" + pontosJog + " vs " + pontosDealer + ").";
                        break;
                    case "WIN_NORMAL":
                        msgResultado = nome + " ganhou (" + pontosJog + " vs " + pontosDealer + "). Ganhou 4 fichas.";
                        break;
                    case "LOSE_NORMAL":
                        msgResultado = nome + " perdeu (" + pontosJog + " vs " + pontosDealer + ").";
                        break;
                }
                writeInTextBox(hora + msgResultado + " (Total: " + fichas + ")");
                break;

            case "WAIT":
                writeInTextBox(hora + parts[2] + " entrou na fila de espera. (Posição: " + parts[3] + ")");

                if (parts[2].equals(meuNome)) {
                    estouSentado = false;
                    this.jButtonModoEsp.setEnabled(true);

                    this.jLabelEspInfo.setVisible(true);

                    this.hitButton.setVisible(false);
                    this.standButton.setVisible(false);
                }
                break;

            case "JOIN":
                writeInTextBox(hora + parts[2] + " sentou à mesa.");
                if (parts[2].equals(meuNome)) {
                    this.lblStatusCentral.setText("Aguarda o início da ronda.");
                }
                mostrarNomeNaMesa(parts[2], parts[3]);

                if (parts[2].equals(meuNome)) {
                    estouSentado = true;
                    this.jButtonModoEsp.setEnabled(true);

                    this.jLabelEspInfo.setVisible(false);
                    this.hitButton.setVisible(true);
                    this.standButton.setVisible(true);
                }
                break;
            case "EXIT": //hora;EXIT;tipoSaida;nome
                if(parts[2].equals("TABLE")) {
                    this.lblStatusCentral.setText(parts[3] + " levantou-se da mesa.");
                } else if (parts[2].equals("QUEUE")) {
                    writeInTextBox(hora + parts[3] + " desistiu de esperar e saiu");
                }
                break;
            case "NOT_ENOUGH_CHIPS":
                if (parts[2].equals(meuNome)) {
                    this.lblStatusCentral.setText("Não tens fichas suficientes para jogar!");
                    this.jLabelFichas.setVisible(false); // temporariamente
                }
                writeInTextBox(hora + parts[2] + " não tem fichas suficientes para jogar!");
                break;

            case "SPECTATORS":
                if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                    this.listaEspetadores = parts[2];
                } else {
                    this.listaEspetadores = "Ninguém";
                }
                break;

            case "NEW_PLAYER":
                writeInTextBox(hora + parts[2] + " saiu da fila de espera e entrou na mesa.");

                mostrarNomeNaMesa(parts[2], parts[3]);

                if (parts[2].equals(meuNome)) {
                    estouSentado = true;
                    this.jButtonModoEsp.setEnabled(true);
                    this.jLabelFichas.setVisible(true);
                    this.jLabelFichas.setText("Fichas: " + parts[4]);

                    this.jLabelEspInfo.setVisible(false);
                    this.hitButton.setVisible(true);
                    this.standButton.setVisible(true);
                }
                break;
            default:
                break;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        standButton = new javax.swing.JButton();
        hitButton = new javax.swing.JButton();
        jButtoneEspLista = new javax.swing.JButton();
        jButtonSair = new javax.swing.JButton();
        jButtonModoEsp = new javax.swing.JButton();
        jLabelDealer = new javax.swing.JLabel();
        jLabelPlayer0 = new javax.swing.JLabel();
        jLabelPlayer2 = new javax.swing.JLabel();
        jLabelPlayer1 = new javax.swing.JLabel();
        lblStatusCentral = new javax.swing.JLabel();
        jLabelPts0 = new javax.swing.JLabel();
        jLabelPts1 = new javax.swing.JLabel();
        jLabelPts2 = new javax.swing.JLabel();
        jLabelFichas = new javax.swing.JLabel();
        jLabelEspInfo = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel1.setBackground(new java.awt.Color(0, 102, 0));
        jPanel1.setLayout(null);

        standButton.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        standButton.setText("STAND");
        standButton.addActionListener(this::standButtonActionPerformed);
        jPanel1.add(standButton);
        standButton.setBounds(420, 630, 110, 24);

        hitButton.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        hitButton.setText("HIT");
        hitButton.addActionListener(this::hitButtonActionPerformed);
        jPanel1.add(hitButton);
        hitButton.setBounds(310, 630, 100, 24);

        jButtoneEspLista.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButtoneEspLista.setText("Espetadores");
        jButtoneEspLista.addActionListener(this::jButtoneEspListaActionPerformed);
        jPanel1.add(jButtoneEspLista);
        jButtoneEspLista.setBounds(713, 10, 150, 24);

        jButtonSair.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButtonSair.setText("Sair");
        jButtonSair.addActionListener(this::jButtonSairActionPerformed);
        jPanel1.add(jButtonSair);
        jButtonSair.setBounds(10, 10, 80, 24);

        jButtonModoEsp.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButtonModoEsp.setText("Modo Espetador");
        jButtonModoEsp.addActionListener(this::jButtonModoEspActionPerformed);
        jPanel1.add(jButtonModoEsp);
        jButtonModoEsp.setBounds(100, 10, 160, 24);

        jLabelDealer.setBackground(new java.awt.Color(255, 255, 255));
        jLabelDealer.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabelDealer.setForeground(new java.awt.Color(255, 255, 255));
        jLabelDealer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelDealer.setText("Dealer");
        jPanel1.add(jLabelDealer);
        jLabelDealer.setBounds(320, 20, 230, 18);

        jLabelPlayer0.setBackground(new java.awt.Color(255, 255, 255));
        jLabelPlayer0.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabelPlayer0.setForeground(new java.awt.Color(255, 255, 255));
        jLabelPlayer0.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelPlayer0.setText("PLayer0");
        jLabelPlayer0.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel1.add(jLabelPlayer0);
        jLabelPlayer0.setBounds(320, 610, 200, 18);

        jLabelPlayer2.setBackground(new java.awt.Color(255, 255, 255));
        jLabelPlayer2.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabelPlayer2.setForeground(new java.awt.Color(255, 255, 255));
        jLabelPlayer2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelPlayer2.setText("PLayer2");
        jPanel1.add(jLabelPlayer2);
        jLabelPlayer2.setBounds(630, 590, 170, 18);

        jLabelPlayer1.setBackground(new java.awt.Color(255, 255, 255));
        jLabelPlayer1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabelPlayer1.setForeground(new java.awt.Color(255, 255, 255));
        jLabelPlayer1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelPlayer1.setText("PLayer1");
        jPanel1.add(jLabelPlayer1);
        jLabelPlayer1.setBounds(50, 590, 180, 18);

        lblStatusCentral.setBackground(new java.awt.Color(255, 255, 255));
        lblStatusCentral.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        lblStatusCentral.setForeground(new java.awt.Color(255, 255, 255));
        lblStatusCentral.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel1.add(lblStatusCentral);
        lblStatusCentral.setBounds(70, 240, 720, 100);

        jLabelPts0.setBackground(new java.awt.Color(255, 255, 255));
        jLabelPts0.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabelPts0.setForeground(new java.awt.Color(255, 255, 255));
        jLabelPts0.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPts0.setText("pontos0");
        jPanel1.add(jLabelPts0);
        jLabelPts0.setBounds(400, 440, 110, 20);

        jLabelPts1.setBackground(new java.awt.Color(255, 255, 255));
        jLabelPts1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabelPts1.setForeground(new java.awt.Color(255, 255, 255));
        jLabelPts1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPts1.setText("pontos1");
        jPanel1.add(jLabelPts1);
        jLabelPts1.setBounds(120, 420, 110, 18);

        jLabelPts2.setBackground(new java.awt.Color(255, 255, 255));
        jLabelPts2.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabelPts2.setForeground(new java.awt.Color(255, 255, 255));
        jLabelPts2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPts2.setText("pontos2");
        jPanel1.add(jLabelPts2);
        jLabelPts2.setBounds(700, 420, 110, 18);

        jLabelFichas.setBackground(new java.awt.Color(255, 255, 255));
        jLabelFichas.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabelFichas.setForeground(new java.awt.Color(255, 255, 255));
        jLabelFichas.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelFichas.setText("fichas");
        jPanel1.add(jLabelFichas);
        jLabelFichas.setBounds(530, 620, 110, 18);

        jLabelEspInfo.setBackground(new java.awt.Color(255, 255, 255));
        jLabelEspInfo.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabelEspInfo.setForeground(new java.awt.Color(255, 255, 255));
        jLabelEspInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelEspInfo.setText("Modo Observador");
        jPanel1.add(jLabelEspInfo);
        jLabelEspInfo.setBounds(560, 10, 140, 20);

        jLabel1.setBackground(new java.awt.Color(0, 153, 0));
        jLabel1.setForeground(new java.awt.Color(0, 153, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel1.add(jLabel1);
        jLabel1.setBounds(0, 0, 880, 670);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 871, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSairActionPerformed
        if (this.connectionThread != null) {
            this.connectionThread.sendCommand("LOGOUT:sair");
            this.connectionThread.disconnect();
        }

        this.dispose();

        java.awt.EventQueue.invokeLater(() -> {
            new TableFrame().setVisible(true);
        });
    }//GEN-LAST:event_jButtonSairActionPerformed

    private void standButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_standButtonActionPerformed
        connectionThread.sendCommand("STAND");
    }//GEN-LAST:event_standButtonActionPerformed

    private void hitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hitButtonActionPerformed
        connectionThread.sendCommand("HIT");
    }//GEN-LAST:event_hitButtonActionPerformed

    private void jButtoneEspListaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtoneEspListaActionPerformed
        // Se não houver ninguém, avisa logo e não faz contas
        if (this.listaEspetadores == null || this.listaEspetadores.trim().isEmpty() || this.listaEspetadores.equals("Ninguem")) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "A fila de espera está vazia.",
                    "Lista de Espetadores",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Separa a string pelos nomes
        String[] nomes = this.listaEspetadores.split(",");

        // Usamos um StringBuilder porque é muito mais rápido e limpo a colar texto que o sinal "+"
        String listaFormatada = "";

        // Constrói a lista final a apontar para cada nome do array
        for (int i = 0; i < nomes.length; i++) {
            int posicao = i + 1; // Para começar no número 1 e não no 0

            listaFormatada += posicao + "º Posição - " + nomes[i].trim() + "\n";
        }

        javax.swing.JOptionPane.showMessageDialog(this,
                "Jogadores na fila de espera:\n\n" + listaFormatada,
                "Lista de Espetadores",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButtoneEspListaActionPerformed

    private void jButtonModoEspActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonModoEspActionPerformed
        if (this.connectionThread != null) {
            this.connectionThread.sendCommand("SPECTATE:");

            this.hitButton.setEnabled(false);
            this.standButton.setEnabled(false);
        }
    }//GEN-LAST:event_jButtonModoEspActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton hitButton;
    private javax.swing.JButton jButtonModoEsp;
    private javax.swing.JButton jButtonSair;
    private javax.swing.JButton jButtoneEspLista;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelDealer;
    private javax.swing.JLabel jLabelEspInfo;
    private javax.swing.JLabel jLabelFichas;
    private javax.swing.JLabel jLabelPlayer0;
    private javax.swing.JLabel jLabelPlayer1;
    private javax.swing.JLabel jLabelPlayer2;
    private javax.swing.JLabel jLabelPts0;
    private javax.swing.JLabel jLabelPts1;
    private javax.swing.JLabel jLabelPts2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lblStatusCentral;
    private javax.swing.JButton standButton;
    // End of variables declaration//GEN-END:variables
}
