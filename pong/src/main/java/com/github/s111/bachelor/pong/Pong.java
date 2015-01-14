package com.github.s111.bachelor.pong;

import com.github.s111.bachelor.pong.server.WebsocketServer;
import org.glassfish.tyrus.server.Server;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import javax.websocket.DeploymentException;

public class Pong extends BasicGame {
    private static Pong game = new Pong("Pong");
    private String message;

    public Pong(String title) {
        super(title);
    }

    public static Pong getGame() {
        return game;
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