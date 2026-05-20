package server.logic;

import server.model.Player;
import server.network.ClientHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BlackjackTable {
    private static final int MAX_PLAYERS = 3;
    private Player[] playing = new Player[MAX_PLAYERS];
    private List<ClientHandler> allClients = new ArrayList<>();
    private Queue<ClientHandler> spectators = new LinkedList<>();

    public BlackjackTable() {
    }


}
