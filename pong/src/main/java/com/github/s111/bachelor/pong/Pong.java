package com.github.s111.bachelor.pong;

import com.github.s111.bachelor.pong.server.WebsocketServer;
import org.glassfish.tyrus.server.Server;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;

public class Pong extends BasicGame {
    private static Pong game = new Pong("Pong");

    private static Session player1;
    private static Session player2;

    private static int players;

    private String message;

    public Pong(String title) {
        super(title);
    }

    public static Pong getGame() {
        return game;
    }

    public static void addPlayer(Session session) {
        int player;

        if (players == 0 || (player1 != null && !player1.isOpen())) {
            player1 = session;

            player = 1;
        } else {
            player2 = session;

            player = 2;
        }

        players++;

        try {
            session.getBasicRemote().sendText("Hello, I'm player " + player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removePlayer() {
        players--;
    }

    public static int getPlayers() {
        return players;
    }

    public void showMessage(String message) {
        this.message = message;
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        Server server = new Server("localhost", 1234, "/", WebsocketServer.class);

        try {
            server.start();
        } catch (DeploymentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {

    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        if (message != null && !message.equals("")) {
            g.drawString(message, 32, 32);
        }
    }
}