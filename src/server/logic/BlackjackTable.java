package server.logic;

import server.model.Card;
import server.model.Deck;
import server.model.Player;
import server.network.ClientHandler;
import server.utils.TableTimer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BlackjackTable {
    private static final int MAX_PLAYERS = 3;
    private Player[] playingNow = new Player[MAX_PLAYERS]; // array para armazenar os jogadores que estao atualmente a jogar na mesa, o índice do array representa a posição do jogador na mesa (0, 1 ou 2)
    private List<ClientHandler> allClients = new ArrayList<>(); // lista de todos os clientes conectados, usada para enviar mensagens para todos os clientes
    private Queue<Player> waitingQueue = new LinkedList<>(); // fila de espera para jogadores que tentam entrar quando a mesa já está cheia

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // variavel para formatar a hora

    private Deck deck = new Deck();

    private List<Card> dealerHand = new ArrayList<>();

    private boolean inRound = false;

    private TableTimer currentTimer = null;
    private Thread timerThread = null;

    private int turnoJogador = -1; // variável para controlar o turno dos jogadores, começa em -1 para indicar que ainda não começou a rodada

    private List<String> listaHistoricoRonda = new ArrayList<>();
    //private String tempMsg;//montar as mensagens para nao repetir
    
    public BlackjackTable() {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            playingNow[i] = null; // inicializar a mesa sem jogadores
        }
    }

    public synchronized boolean addPlayer(String name, ClientHandler handler) {
        if (nameExists(name)) {
            return false;
        }

        Player newPlayer = new Player(name, handler);
        allClients.add(handler);
        boolean canSit = false;
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (playingNow[i] == null) { // procurar lugar vazio
                playingNow[i] = newPlayer;
                canSit = true;
                sendMessageToAll("JOIN;" + name + ";" + i);

                if (!inRound) { //se nao estiver numa ronda comeca
                    sendMessageToAll("INFO;A aguardar jogadores, o jogo vai começar em 10 segundos.");
                    startTimer(10, "ROUND");
                }
                break;
            }
        }

        if (!canSit) {
            waitingQueue.add(newPlayer);    
            sendMessageToAll("WAIT;" + name + ";" + waitingQueue.size());
            enviarListaEspetadores();
        }
       return true; // se o nome do jogador já existe, retorna false. Caso contrário, adiciona o jogador e retorna true.
    }
    
    public synchronized void enviarHistorico(ClientHandler handler) {
        if (inRound) {
            //para atualizar a malta que entrou a meio
            for (String msg : listaHistoricoRonda) {
                String currentTime = LocalDateTime.now().format(timeFormatter); // timestamp da hora atual
                String formattedMessage = "[" + currentTime + "];" + msg; // mensagem formatada com timestamp
                handler.sendMessage(formattedMessage); 
                
                System.out.println("em falta->" + formattedMessage);
            }
        }
    }

    // metodo para remover mesmo sem o nome do jogador para usar quando um cliente se desconecta sem fazer logout
    public synchronized void removeClient(ClientHandler handler) {
        allClients.remove(handler);

        if (handler.getUsername() != null) {
            removePlayer(handler.getUsername());
        }
    }

    public synchronized void removePlayer(String name) {

        ClientHandler clientToRemove = null;
        for (ClientHandler c : allClients) { //remover da lista de todos
            if (c.getUsername() != null && c.getUsername().equals(name)) {
                clientToRemove = c;
                break;
            }
        }
        allClients.remove(clientToRemove);

        boolean wasPlaying = false;
        for (int i = 0; i < MAX_PLAYERS; i++) { //ver se estava a jogar
            if (playingNow[i] != null && playingNow[i].getNome().equals(name)) { // se a posicao na mesa nao for nula e o nome do jogador for igual ao nome recebido
                playingNow[i] = null; //meter o lugar dele vazio
                wasPlaying = true;
                sendMessageToAll("EXIT;TABLE" + name);
                
                // se esta pessoa na ronda dela ja saiu passa a frente
                if (inRound && turnoJogador == i) {
                    stopTimer();
                    passarTurno();
                }
                break;
            }
        }

        if (!wasPlaying) { //nao estava a jogar, estava na lista de espera
            Player playerToRemove = null;
            for (Player p : waitingQueue) {
                if (p.getNome() != null && p.getNome().equals(name)) {
                    playerToRemove = p;
                    break;
                }
            }
            waitingQueue.remove(playerToRemove);
            sendMessageToAll("EXIT;QUEUE" + name);

            enviarListaEspetadores();

        } else { //estava a jogar entao tem que entrar um novo player
            putSpectatorToTable();
        }
    }

    public synchronized boolean nameExists(String name) {//verificar se existe o nome nas listas
        for (Player player : playingNow) {

            if (player != null && player.getNome().equals(name)) {
                return true;
            }
        }

        for (Player player : waitingQueue) {
            if (player != null && player.getNome().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void sendMessageToAll(String message) {
        String currentTime = LocalDateTime.now().format(timeFormatter); // timestamp da hora atual
        String formattedMessage = "[" + currentTime + "];" + message; // mensagem formatada com timestamp

        for (ClientHandler client : allClients) {
            client.sendMessage(formattedMessage);
        }
    }

    private void putSpectatorToTable() {
        boolean filaMudou = false;
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (playingNow[i] == null && !waitingQueue.isEmpty()) {//se tiver um lugar vazio e se houver pessoas a espera
                Player next = waitingQueue.poll();//pessoa que esta a frente na fila
                playingNow[i] = next;
                next.setFichas(10);
                sendMessageToAll("NEW_PLAYER;" + next.getNome() + ";" + i + ";" + next.getFichas());
                filaMudou = true;
            }
        }

        if (filaMudou) {
            enviarListaEspetadores();
        }
    }

    public synchronized void beginRound() {
        if (inRound) {//se ainda tiver a correr a ronda
            return;
        }
        
        listaHistoricoRonda.clear();
        
        sendMessageToAll("BEGIN;Começou uma nova Ronda!");

        // corrigido um bug que acontecia de nao cobrar a quem entra pois o for ja tinha passado e o jogador entrava a meio da ronda, entao agora tem 2 ciclos,
        // o primeiro para verificar se os jogadores que estao a jogar tem fichas suficientes para jogar e remover os que nao tem,
        // e o segundo ciclo para cobrar a aposta a quem esta atualmente sentado
        for (int i = 0; i < MAX_PLAYERS; i++) {
            Player player = playingNow[i];
            if (player != null && player.getFichas() < 2) {
                sendMessageToAll("NOT_ENOUGH_CHIPS;" + player.getNome());
                //removePlayer(player.getNome());
                playingNow[i] = null;
                sendMessageToAll("EXIT;TABLE" + player.getNome());

                waitingQueue.add(player); // O player passa a espetador
            }
        }

        enviarListaEspetadores();

        putSpectatorToTable();

        // for para cobrar a aposta a quem está atualmente sentado
        for (int i = 0; i < MAX_PLAYERS; i++) {
            Player player = playingNow[i];
            if (player != null) {
                player.setFichas(player.getFichas() - 2);
                sendMessageToAll("ROUND_START;" + player.getNome() + ";" + player.getFichas());
            }
        }

        deck.shuffle();
        dealerHand.clear();

        for (Player player : playingNow){ //limpar as maos antes
            if (player!=null){
                player.clearMao();
            }
        }

        inRound = true;

        for (int volta = 0; volta < 2; volta++) { // ciclo for para dar as 2 cartas iniciais a cada jogador e ao dealer
            for (int i = 0; i < MAX_PLAYERS; i++) { 
                Player player = playingNow[i];

                if (player != null) {
                    Card card = deck.deal();
                    player.addCard(card);
                    String tempMsg = "PLAYER_CARD;" + player.getNome() + ";" + card.getName() + ";" + player.calculateHandValue() + ";" + i;
           
                    listaHistoricoRonda.add(tempMsg);//para depois se entraem a meio
                    sendMessageToAll(tempMsg);
                }
            }

            Card dealerCard = deck.deal();
            dealerHand.add(dealerCard);
            
            if (volta == 0) {//comando;carta;virada ou nao
                String tempMsg = "DEALER_CARD;;face down; ";
                listaHistoricoRonda.add(tempMsg);
                sendMessageToAll(tempMsg); // bv é o asset da carta virada para baixo no cliente (bv = back view)
            } else {
                String tempMsg = "DEALER_CARD;"+ dealerCard.getName() + ";face up;" + dealerCard.getValue();
                listaHistoricoRonda.add(tempMsg);
                sendMessageToAll(tempMsg);//levar so o vcalor da carta senao mostra o total escondido
            }
        }

        sendMessageToAll("INFO;As cartas foram dadas.");
        passarTurno();
    }

    public synchronized void passarTurno() {
        turnoJogador++;

        while (turnoJogador < MAX_PLAYERS) { //jogadores [0,1,2]
            Player player = playingNow[turnoJogador];

            if (player != null && !player.getMao().isEmpty()) {
                int pontos = player.calculateHandValue();

                if (pontos == 21) {
                    sendMessageToAll("INFO;O " + player.getNome() + " fez Blackjack!");
                    sendMessageToAll("STAND;" + player.getNome() + ";21");
                } else {
                    sendMessageToAll("TURN;" + player.getNome());

                    startTimer(20, "PLAY");
                    return;
                }
            }
            turnoJogador++;
        }
        // se saiu do loop quer dizer que todos jogaram e é a vez do dealer jogar
        dealerTurn();
    }

    public synchronized void requestHit(String playerName) {

        if (turnoJogador == -1 || turnoJogador >= MAX_PLAYERS) { //se nao for ele a jogar
            return;
        }

        Player currentPlayer = playingNow[turnoJogador];

        if (currentPlayer == null || !currentPlayer.getNome().equals(playerName)) {
            return; // ignora se o jogador que pediu o hit não é o jogador atual
        }

        stopTimer();

        Card card = deck.deal();
        currentPlayer.addCard(card);
        //Comando;NomeDoJogador;NomeDaCarta;TotalDePontos;posicaoDoArray
        String tempMsg = "PLAYER_CARD;" + currentPlayer.getNome() + ";" + card.getName() + ";" + currentPlayer.calculateHandValue() +";" + turnoJogador;
        listaHistoricoRonda.add(tempMsg);
        sendMessageToAll(tempMsg);

        if (currentPlayer.calculateHandValue() > 21) {
            sendMessageToAll("BUST;" + currentPlayer.getNome() + ";" + currentPlayer.calculateHandValue());
            passarTurno();
        } else {
            startTimer(20, "PLAY");
        }
    }

    public synchronized void requestStand(String playerName) {
        System.out.println("Request STAND");
        if (turnoJogador == -1  || turnoJogador >= MAX_PLAYERS) { //se nao for o turno dele
            return;
        }
        Player currentPlayer = playingNow[turnoJogador];

        if (currentPlayer == null || !currentPlayer.getNome().equals(playerName)) {
            return;
        }

        stopTimer();

        sendMessageToAll("STAND;" + currentPlayer.getNome() + ";" + currentPlayer.calculateHandValue());

        passarTurno();
    }

    // a diferenca deste metodo para o metodo na classe Player é que recebe uma lista de cartas como parametro,
    // para calcular o valor da mão do dealer, enquanto o metodo na classe Player calcula o valor da mão do jogador usando a lista de cartas na classe Player
    // foi a forma que eu arranjei nao é a mais bonita mas yh :). Tambem tive preguica de fazer o metodo na classe Player mais generico para receber uma lista de cartas como parametro, tinha que estar andar a mudar o metodo na classe Player e em varios pontos do codigo onde é chamado,
    // entao preferi fazer um metodo separado para calcular o valor da mao do dealer usando a lista de cartas do dealer como parametro
    private int calculateHandListValue(List<Card> cards) {
        int total = 0;
        int aces = 0;

        for (Card card : cards) {
            total += card.getValue();

            if (card.getValue() == 11) { // se a carta for um ás, incrementa o contador de ases
                aces++;
            }
        }

        while (total > 21 && aces > 0) { // enquanto o valor total for maior que 21 e houver ases, subtrai 10 do total e decrementa o contador de ases
            total -= 10;
            aces--;
        }

        return total;
    }

    private synchronized void dealerTurn() {
        turnoJogador = -1; // sendo o dealer a jogar o -1 serve para bloquear as ações dos jogadores
        System.out.println("DEALER TURN");

        sendMessageToAll("DEALER_TURN;Vez do Dealer a jogar.");
        Card downCard = dealerHand.get(0); // carta virada para baixo
        String tempMsg = "DEALER_CARD;" + downCard.getName() + ";face down reveal;" + calculateHandListValue(dealerHand);
        listaHistoricoRonda.add(tempMsg);
        sendMessageToAll(tempMsg); //substiutuir a que estava para baixo

        int dealerHandValue = calculateHandListValue(dealerHand); // calcular antes de verificar para caso o dealer tenha blackjack

        while (dealerHandValue < 17) { // o dealer é obrigado a pedir enquanto tiver uma mao com valor menor que 17
            Card card = deck.deal();
            dealerHand.add(card);
            tempMsg = "DEALER_CARD;" + card.getName() + ";face up;"+calculateHandListValue(dealerHand);
            listaHistoricoRonda.add(tempMsg);
            sendMessageToAll(tempMsg);


            dealerHandValue = calculateHandListValue(dealerHand);
        }

        if (dealerHandValue > 21) {
            sendMessageToAll("DEALER_BUST;" + dealerHandValue);
        } else {
            sendMessageToAll("DEALER_STAND;" + dealerHandValue);
        }
        // aqui vai o metodo para comparar as maos dos jogadores com a do dealer
        calculateResults();
    }

    private synchronized void calculateResults() {

        int dealerHandValue = calculateHandListValue(dealerHand);
        boolean dealerBusted = dealerHandValue > 21;

        boolean dealerHasBlackjack = dealerHandValue == 21 && dealerHand.size() == 2; // se o dealer tem um Blackjack natural (21 com as duas cartas iniciais)

        for (int i = 0; i < MAX_PLAYERS; i++) {
            Player currentPlayer = playingNow[i];

            if (currentPlayer != null && !currentPlayer.getMao().isEmpty()) {
                int playerHandValue = currentPlayer.calculateHandValue();
                System.out.println("MAO DO PLAYER " + playerHandValue);

                boolean playerHasBlackjack = playerHandValue == 21 && currentPlayer.getMao().size() == 2;

                if (playerHandValue > 21) {
                    sendMessageToAll("RESULT;LOSE_BUST;" + currentPlayer.getNome() + ";" + playerHandValue + ";0;" + currentPlayer.getFichas());
                    // como as fichas já foram descontadas no início da rodada, não é necessário fazer nada aqui para retirar fichas do jogador

                } else if (playerHasBlackjack && !dealerHasBlackjack) {
                    currentPlayer.setFichas(currentPlayer.getFichas() + 5); // Blackjack natural recebe 3:2, ou seja, o jogador recebe 5 fichas (2 fichas da aposta + 3 fichas de lucro)
                    sendMessageToAll("RESULT;WIN_BLACKJACK;" + currentPlayer.getNome() + ";" + playerHandValue + ";" + dealerHandValue + ";" + currentPlayer.getFichas());

                } else if (playerHasBlackjack && dealerHasBlackjack) {
                    currentPlayer.setFichas(currentPlayer.getFichas() + 2); // empate com Blackjack, o jogador recebe o valor da aposta de volta (2 fichas)
                    sendMessageToAll("RESULT;TIE_BLACKJACK;" + currentPlayer.getNome() + ";" + playerHandValue + ";" + dealerHandValue + ";" + currentPlayer.getFichas());

                } else if (dealerBusted) {
                    currentPlayer.setFichas(currentPlayer.getFichas() + 4); // vitoria normal recebe 1:1, ou seja, o jogador recebe 4 fichas (2 fichas da aposta + 2 fichas de lucro)
                    sendMessageToAll("RESULT;WIN_DEALER_BUST;" + currentPlayer.getNome() + ";" + playerHandValue + ";" + dealerHandValue + ";" + currentPlayer.getFichas());

                } else if (playerHandValue == dealerHandValue) {
                    currentPlayer.setFichas(currentPlayer.getFichas() + 2); // empate normal, o jogador recebe o valor da aposta de volta (2 fichas)
                    sendMessageToAll("RESULT;TIE_NORMAL;" + currentPlayer.getNome() + ";" + playerHandValue + ";" + dealerHandValue + ";" + currentPlayer.getFichas());

                } else if (playerHandValue > dealerHandValue){ //se nos tivermos mais que o dealer mas nao for blackjack
                    currentPlayer.setFichas(currentPlayer.getFichas() + 4);
                    sendMessageToAll("RESULT;WIN_NORMAL;" + currentPlayer.getNome() + ";" + playerHandValue + ";" + dealerHandValue + ";" + currentPlayer.getFichas());

                } else { // se os pontos do jogador forem menores que os do dealer, o jogador perde a aposta
                    sendMessageToAll("RESULT;LOSE_NORMAL;" + currentPlayer.getNome() + ";" + playerHandValue + ";" + dealerHandValue + ";" + currentPlayer.getFichas());                }
            }
        }
        inRound = false; // depois de calcular os resultados da rodada, a variável inRound é setada para false para permitir que uma nova rodada seja iniciada quando o dealer terminar de jogar

        startTimer(10, "ROUND");
    }

    public void stopTimer() {
        if (currentTimer != null) {
            currentTimer.stop();
            currentTimer = null;
        }
        if (timerThread != null) {
            timerThread.interrupt();
            timerThread = null;
        }
    }

    public void startTimer(int seconds, String type) {
        stopTimer();

        currentTimer = new TableTimer(seconds, type, this);
        timerThread = new Thread(currentTimer);
        timerThread.start();
    }

    public synchronized void processEndTimer(String type) {
        switch (type) {
            case "ROUND":
                sendMessageToAll("INFO;O tempo da Ronda acabou.");
                beginRound();
                break;

            case "PLAY":
                if (turnoJogador != -1 && playingNow[turnoJogador] != null) {
                    Player currentPlayer = playingNow[turnoJogador];
                    sendMessageToAll("WARNING;TIMEOUT;" + currentPlayer.getNome());
                    requestStand(currentPlayer.getNome());
                } else {
                    passarTurno();
                }
                break;
        }
    }

    public void enviarListaEspetadores() {
        StringBuilder lista = new StringBuilder();
        
        if (waitingQueue.isEmpty()) {
            sendMessageToAll("SPECTATORS;Ninguem");
            return;
        }
        
        for (Player p : waitingQueue) {
            lista.append(p.getNome()).append(",");
        }

        sendMessageToAll("SPECTATORS;" + lista.toString());
    }

    public synchronized void makeSpectator(String name) {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (playingNow[i] != null && playingNow[i].getNome().equals(name)) {
                Player p  = playingNow[i];

                playingNow[i] = null;
                sendMessageToAll("EXIT;TABLE" + name);

                waitingQueue.add(p);
                sendMessageToAll("WAIT;" + name + ";" + waitingQueue.size());
                enviarListaEspetadores();

                if (inRound && turnoJogador == i) {
                    stopTimer();
                    passarTurno();
                }

                putSpectatorToTable();
                break;
            }
        }
    }
}
