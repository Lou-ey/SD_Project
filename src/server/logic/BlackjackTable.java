package server.logic;

import server.model.Card;
import server.model.Deck;
import server.model.Player;
import server.network.ClientHandler;
import server.utils.TableTimer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BlackjackTable {
    private static final int MAX_PLAYERS = 3;
    private Player[] playingNow = new Player[MAX_PLAYERS];
    private List<ClientHandler> allClients = new ArrayList<>();
    private Queue<Player> waitingQueue = new LinkedList<>();

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // variavel para formatar a hora

    private Deck deck = new Deck();

    private List<Card> dealerHand = new ArrayList<>();

    private boolean inRound = false;

    private TableTimer currentTimer = null;
    private Thread timerThread = null;

    private int turnoJogador = -1;

    public BlackjackTable() {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            playingNow[i] = null; // inicializar a mesa sem jogadores
        }
    }

    public boolean addPlayer(String name, ClientHandler handler) {
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
                sendMessageToAll("JOIN:" + name + " joined the table and took a seat.");
                break;
            }
        }

        if (!canSit) {
            waitingQueue.add(newPlayer);
            sendMessageToAll("WAIT:" + name + " joined the table as spectator. Position: " + waitingQueue.size());
        }
        return true; // se o nome do jogador já existe, retorna false. Caso contrário, adiciona o jogador e retorna true.
    }

    public void removePlayer(String name) {
        // como em java nao é possivel remover um elemento de uma lista enquanto se percorre a mesma, é necessário usar um iterador para evitar o ConcurrentModificationException
        Iterator<ClientHandler> itClients = allClients.iterator(); // iterador para percorrer a lista de clientes para podermos remover durante o loop
        while (itClients.hasNext()) { // enquanto houver clientes na lista
            ClientHandler clientHandler = itClients.next(); // obter o handler do cliente
            if (clientHandler.getUsername() != null && clientHandler.getUsername().equals(name)) {
                itClients.remove();
                break;
            }
        }

        boolean wasPlaying = false;
        for (int i = 0; i < MAX_PLAYERS; i++) { // vai percorrer os jogadores a jogar
            if (playingNow[i] != null && playingNow[i].getNome().equals(name)) { // se a posicao na mesa nao for nula e o nome do jogador for igual ao nome recebido
                playingNow[i] = null;
                wasPlaying = true;
                sendMessageToAll("EXIT:" + name + " left the table.");
                break;
            }
        }

        if (!wasPlaying) {
            Iterator<Player> itWaiting = waitingQueue.iterator(); // iterador para percorrer a fila de espera para poder remover durante o loop
            while (itWaiting.hasNext()) {
                Player player = itWaiting.next();
                if (player.getNome().equals(name)) {
                    itWaiting.remove();
                    sendMessageToAll("EXIT:" + name + " left the table.");
                    break;
                }
            }
        } else { //
            // metodo de colocar o primeiro da fila de espera a jogar
            putSpectatorToTable();
        }
    }

    public boolean nameExists(String name) {
        for (Player player : playingNow) {
            if (player.getNome().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void sendMessageToAll(String message) {
        String currentTime = LocalDateTime.now().format(timeFormatter); // timestamp da hora atual
        String formattedMessage = "[" + currentTime + "] " + message; // mensagem formatada com timestamp

        for (ClientHandler client : allClients) {
            client.sendMessage(formattedMessage);
        }
    }

    private void putSpectatorToTable() {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (playingNow[i] == null && !waitingQueue.isEmpty()) {
                Player next = waitingQueue.poll();
                next.setFichas(10);
                sendMessageToAll("NEW_PLAYER:" + next.getNome() + " is now playing at the table.");
            }
        }
    }

    public void beginRound() {
        if (inRound) {
            return;
        }

        sendMessageToAll("BEGIN:A new round is starting!");

        for (int i = 0; i < MAX_PLAYERS; i++) {
            Player player = playingNow[i];
            if (player != null) {
                if (player.getFichas() < 2) {
                    sendMessageToAll("NOT_ENOUGH_CHIPS:" + player.getNome() + " does not have enough chips to play and will be removed from the table.");}
                    removePlayer(player.getNome());
                } else {
                    player.setFichas(player.getFichas() - 2); // descontar as fichas para jogar
                    sendMessageToAll("ROUND_START:" + player.getNome() + " has joined the round with 2 chips.");
            }
        }

        deck.shuffle();
        dealerHand.clear();
        inRound = true;

        for (int volta = 0; volta < 2; volta++) { // for para dar as 2 cartas iniciais a cada jogador e ao dealer
            for (int i = 1; i < MAX_PLAYERS; i++) {
                Player player = playingNow[i];

                if (player != null) {
                    Card card = deck.deal();
                    player.addCard(card);
                    sendMessageToAll("PLAYER_CARD:" + player.getNome() + " received " + card.getName() + ".");
                }
            }

            Card dealerCard = deck.deal();
            dealerHand.add(dealerCard);
            if (volta == 0) {
                sendMessageToAll("DEALER_CARD:Dealer received " + dealerCard.getName() + " (face up).");
            } else {
                sendMessageToAll("DEALER_CARD:bv"); // bv é o asset da carta virada para baixo no cliente (bv = back view)
            }
        }

        sendMessageToAll("INFO:Cards have been dealt.");
    }

    public void passarTurno() {
        turnoJogador++;

        while (turnoJogador < MAX_PLAYERS) {
            Player player = playingNow[turnoJogador];

            if (player != null) {
                sendMessageToAll("TURN:" + player.getNome() + "'s turn.");

                startTimer(20, "PLAY");
                return;
            }
            turnoJogador++;
        }
        // se saiu do loop quer dizer que todos jogaram e é a vez do dealer jogar
    }

    public void requestHit(String playerName) {

        if (turnoJogador == -1 || turnoJogador >= MAX_PLAYERS) {
            return;
        }

        Player currentPlayer = playingNow[turnoJogador];

        if (currentPlayer == null || !currentPlayer.getNome().equals(playerName)) {
            return; // ignora se o jogador que pediu o hit não é o jogador atual
        }

        stopTimer();

        Card card = deck.deal();
        currentPlayer.addCard(card);
        sendMessageToAll("PLAYER_CARD:" + currentPlayer.getNome() + " received " + card.getName() + ".");

        if (currentPlayer.calculateHandValue() > 21) {
            sendMessageToAll("BUST:" + currentPlayer.getNome() + " has busted!");
            passarTurno();
        } else {
            startTimer(20, "PLAY");
        }
    }

    public void requestStand(String playerName) {
        if (turnoJogador == -1  || turnoJogador >= MAX_PLAYERS) {
            return;
        }
        Player currentPlayer = playingNow[turnoJogador];

        if (currentPlayer == null || !currentPlayer.getNome().equals(playerName)) {
            return;
        }

        stopTimer();

        sendMessageToAll("STAND:" + currentPlayer.getNome() + " has chosen to stand with a hand value of " + currentPlayer.calculateHandValue() + ".");

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

            if (card.getName().endsWith("1")) { // se a carta for um ás, incrementa o contador de ases
                aces++;
            }
        }

        while (total > 21 && aces > 0) { // enquanto o valor total for maior que 21 e houver ases, subtrai 10 do total e decrementa o contador de ases
            total -= 10;
            aces--;
        }

        return total;
    }

    private void dealerTurn() {
        turnoJogador = -1; // sendo o dealer a jogar o -1 serve para bloquear as ações dos jogadores

        sendMessageToAll("DEALER_TURN:Dealer's turn to play.");
        Card downCard = dealerHand.get(1); // carta virada para baixo
        sendMessageToAll("DEALER_CARD:Dealer's down card is " + downCard.getName() + ".");

        int dealerHandValue = calculateHandListValue(dealerHand); // calcular antes de verificar para caso o dealer tenha blackjack

        while (dealerHandValue < 17) { // o dealer é obrigado a pedir enquanto tiver uma mao com valor menor que 17
            Card card = deck.deal();
            dealerHand.add(card);
            sendMessageToAll("DEALER_CARD:Dealer received " + card.getName() + ".");

            dealerHandValue = calculateHandListValue(dealerHand);
        }

        if (dealerHandValue > 21) {
            sendMessageToAll("DEALER_BUST:Dealer has busted with a hand value of " + dealerHandValue + "!");
        } else {
            sendMessageToAll("DEALER_STAND:Dealer stands with a hand value of " + dealerHandValue + ".");
        }
        // aqui vai o metodo para comparar as maos dos jogadores com a do dealer
    }

    private void calculateResults() {
        sendMessageToAll("RESULTS:Calculating results for the round.");

        int dealerHandValue = calculateHandListValue(dealerHand);
        boolean dealerBusted = dealerHandValue > 21;

        boolean dealerHasBlackjack = dealerHandValue == 21 && dealerHand.size() == 2; // se o dealer tem um Blackjack natural (21 com as duas cartas iniciais)

        for (int i = 0; i < MAX_PLAYERS; i++) {
            Player currentPlayer = playingNow[i];

            if (currentPlayer != null) {
                int playerHandValue = currentPlayer.calculateHandValue();

                boolean playerHasBlackjack = playerHandValue == 21 && currentPlayer.getMao().size() == 2;

                if (playerHandValue > 21) {
                    sendMessageToAll("RESULT:" + currentPlayer.getNome() + " busted and loses with a hand value of " + playerHandValue + "! Current chips: " + currentPlayer.getFichas());
                    // como as fichas já foram descontadas no início da rodada, não é necessário fazer nada aqui para retirar fichas do jogador
                } else if (playerHasBlackjack && !dealerHasBlackjack) {
                    currentPlayer.setFichas(currentPlayer.getFichas() + 5); // Blackjack natural recebe 3:2, ou seja, o jogador recebe 5 fichas (2 fichas da aposta + 3 fichas de lucro)
                    sendMessageToAll("RESULT:" + currentPlayer.getNome() + " wins with a Blackjack and receives 5 chips! Current chips: " + currentPlayer.getFichas());
                } else if (playerHasBlackjack && dealerHasBlackjack) {
                    currentPlayer.setFichas(currentPlayer.getFichas() + 2); // empate com Blackjack, o jogador recebe o valor da aposta de volta (2 fichas)
                    sendMessageToAll("RESULT:" + currentPlayer.getNome() + " ties with the dealer with a Blackjack and receives 2 chips back. Current chips: " + currentPlayer.getFichas());
                } else if (dealerBusted) {
                    currentPlayer.setFichas(currentPlayer.getFichas() + 4); // vitoria normal recebe 1:1, ou seja, o jogador recebe 4 fichas (2 fichas da aposta + 2 fichas de lucro)
                    sendMessageToAll("RESULT:" + currentPlayer.getNome() + " wins as the dealer busted and receives 4 chips! Current chips: " + currentPlayer.getFichas());
                } else if (playerHandValue == dealerHandValue) {
                    currentPlayer.setFichas(currentPlayer.getFichas() + 2); // empate normal, o jogador recebe o valor da aposta de volta (2 fichas)
                    sendMessageToAll("RESULT:" + currentPlayer.getNome() + " ties with the dealer and receives 2 chips back. Current chips: " + currentPlayer.getFichas());
                } else { // se os pontos do jogador forem menores que os do dealer, o jogador perde a aposta
                    sendMessageToAll("RESULT:" + currentPlayer.getNome() + " loses with a hand value of " + playerHandValue + " against the dealer's " + dealerHandValue + ". Current chips: " + currentPlayer.getFichas());
                }
            }

            inRound = false; // depois de calcular os resultados da rodada, a variável inRound é setada para false para permitir que uma nova rodada seja iniciada quando o dealer terminar de jogar

            sendMessageToAll("INFO:A new round will begin in 10 seconds...");
            startTimer(10, "ROUND");
        }
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

    public void processEndTimer(String type) {
        switch (type) {
            case "ROUND":
                sendMessageToAll("INFO:Round timer ended.");
                beginRound();
                break;
            case "PLAY":
                if (turnoJogador != -1 && playingNow[turnoJogador] != null) {
                    Player currentPlayer = playingNow[turnoJogador];
                    sendMessageToAll("WARNING:Time for " + currentPlayer.getNome() + " to play has ended. Automatic STAND applied.");
                    requestStand(currentPlayer.getNome());
                }
                break;
        }
    }
}
