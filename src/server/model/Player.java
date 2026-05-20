package server.model;

import server.network.ClientHandler;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String nome;
    private int fichas;
    private List<Card> mao;
    private ClientHandler clientHandler;

    public Player(String nome, ClientHandler clientHandler) {
        this.nome = nome;
        this.fichas = 10;
        this.mao = new ArrayList<>();
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
        return mao;
    }

    public void setMao(List<Card> mao) {
        this.mao = mao;
    }
}
