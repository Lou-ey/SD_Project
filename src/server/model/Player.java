package server.model;

import server.network.ClientHandler;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String nome;
    private int fichas;
    private List<Card> hand;
    private ClientHandler clientHandler;

    public Player(String nome, ClientHandler clientHandler) {
        this.nome = nome;
        this.fichas = 10;
        this.hand = new ArrayList<>();
        this.clientHandler = clientHandler;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public int getFichas() {
        return fichas;
    }

    public void setFichas(int fichas) {
        this.fichas = fichas;
    }

    public List<Card> getMao() {
        return hand;
    }

    public void setMao(List<Card> mao) {
        this.hand = mao;
    }

    public void addCard(Card card){
        hand.add(card);
    }

    public void clearMao(){
        hand.clear();
    }

    public int calculateHandValue() {
        int total = 0;
        int aces = 0;

        for (Card card : hand) {
            total += card.getValue();

            if (card.getName().endsWith("1")) {
                aces++;
            }

            while(total > 21 && aces > 0) {
                total -= 10;
                aces--;
            }
        }
        return total;
    }
}
