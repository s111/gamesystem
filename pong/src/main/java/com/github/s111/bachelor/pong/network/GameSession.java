package com.github.s111.bachelor.pong.network;

import com.github.s111.bachelor.pong.game.Pong;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;

public class GameSession {
    private final Pong game;

    private Session player1;
    private Session player2;

    public GameSession(Pong game) throws DeploymentException {
        this.game = game;

        Server server = new Server("localhost", 1234, "/", WebsocketServer.class);
        server.start();
    }

    public void onOpen(Session session) {
        boolean player1Active = player1 == null || !player1.isOpen();
        boolean player2Active = player2 == null || !player2.isOpen();

        if (player1Active) {
            player1 = session;
        } else if (player2Active) {
            player2 = session;
        } else {
            closeConnection(session);
        }
    }

    public void closeConnection(Session session) {
        try {
            RemoteEndpoint.Basic remote = session.getBasicRemote();
            remote.sendText("Already got 2 players");

            session.close();
        } catch (IOException e) {
            // Just ignore the exception as we are dropping the client
        }
    }

    public void onMessage(Session session, float message) throws IOException {
        if (player1.equals(session)) {
            game.movePlayer1(message);
        } else {
            game.movePlayer2(message);
        }
    }

    public void broadcastScore(int player1score, int player2score) {
        String score = "[" + player1score + ", " + player2score + "]";

        try {
            if (player1 != null) {
                RemoteEndpoint.Basic remote1 = player1.getBasicRemote();
                remote1.sendText(score);
            }

            if (player2 != null) {
                RemoteEndpoint.Basic remote2 = player2.getBasicRemote();
                remote2.sendText(score);
            }
        } catch (IOException e) {
            // Ignore exception, the score is updated next time and is also displayed on the game screen
        }
    }
}
