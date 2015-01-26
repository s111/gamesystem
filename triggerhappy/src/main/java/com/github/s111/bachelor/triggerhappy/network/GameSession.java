package com.github.s111.bachelor.triggerhappy.network;

import com.github.s111.bachelor.triggerhappy.game.Triggerhappy;

import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;

public class GameSession {
    private final Triggerhappy game;
    private Session player1;
    private Session player2;
    public GameSession(Triggerhappy game) throws DeploymentException {
        this.game = game;
        Server server = new Server("localhost", 1234, "/", null, WebsocketServer.class);
        server.start();
    }
    public void onOpen(Session session) throws IOException {
        boolean player1NotActive = player1 == null || !player1.isOpen();
        boolean player2NotActive = player2 == null || !player2.isOpen();
        if (player1NotActive) {
            player1 = session;
        } else if (player2NotActive) {
            player2 = session;
        } else {
            closeConnection(session);
            return;
        }
        RemoteEndpoint.Basic remote = session.getBasicRemote();
        remote.sendPing(ByteBuffer.wrap("".getBytes()));
    }
    public void closeConnection(Session session) throws IOException {
        RemoteEndpoint.Basic remote = session.getBasicRemote();
        remote.sendText("Already got 2 players");
        session.close();
    }
    public void onMessage(Session session, String message) throws IOException {
        int position;
        try {
            position = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            position = -1;
        }
        if (player1.equals(session)) {
            game.shootEnemy(1, position);
        } else {
            game.shootEnemy(2, position);
        }
    }
    public void broadcastScore(int player1score, int player2score) {
        String score = "[" + player1score + ", " + player2score + "]";
        try {
            if (player1 != null && player1.isOpen()) {
                RemoteEndpoint.Basic remote1 = player1.getBasicRemote();
                remote1.sendText(score);
            }
            if (player2 != null && player2.isOpen()) {
                RemoteEndpoint.Basic remote2 = player2.getBasicRemote();
                remote2.sendText(score);
            }
        } catch (IOException e) {
// Ignore exception, the score is updated next time and is also displayed on the game screen
        }
    }
}
