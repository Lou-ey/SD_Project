package server.logic;

import server.model.Card;
import server.model.Deck;
import server.model.Player;
import server.network.ClientHandler;
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
                sendMessageToAll("JOIN: " + name + " joined the table and took a seat.");
                break;
            }
        }

        if (!canSit) {
            waitingQueue.add(newPlayer);
            sendMessageToAll("WAIT: " + name + " joined the table as spectator. Position: " + waitingQueue.size());
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
                sendMessageToAll("EXIT: " + name + " left the table.");
                break;
            }
        }

        if (!wasPlaying) {
            Iterator<Player> itWaiting = waitingQueue.iterator(); // iterador para percorrer a fila de espera para poder remover durante o loop
            while (itWaiting.hasNext()) {
                Player player = itWaiting.next();
                if (player.getNome().equals(name)) {
                    itWaiting.remove();
                    sendMessageToAll("EXIT: " + name + " left the table.");
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
                sendMessageToAll("NEW_PLAYER: " + next.getNome() + " is now playing at the table.");
            }
        }
    }
}
