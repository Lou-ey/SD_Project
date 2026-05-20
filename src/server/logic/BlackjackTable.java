package server.logic;

import server.model.Player;
import server.network.ClientHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BlackjackTable {
    private static final int MAX_PLAYERS = 3;
    private Player[] playingNow = new Player[MAX_PLAYERS];
    private List<ClientHandler> allClients = new ArrayList<>();
    private Queue<Player> spectators = new LinkedList<>();

    public BlackjackTable() {
    }

    public boolean addPlayer(String name, ClientHandler handler) {
        if (nameExists(name)) {
            return false;
        }

        Player newPlayer = new Player(name, handler);
        allClients.add(handler);

        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (playingNow[i] == null) { // procurar lugar vazio
                playingNow[i] = newPlayer;
                // enviar mensagem para todos
                break;
            }
        }
        return true;
    }

    public boolean nameExists(String name) {
        for (Player player : playingNow) {
            if (player.getNome().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
