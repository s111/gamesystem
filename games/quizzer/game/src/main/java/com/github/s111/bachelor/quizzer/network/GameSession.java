package com.github.s111.bachelor.quizzer.network;

import com.github.s111.bachelor.quizzer.game.Quizzer;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;

public class GameSession {
    private final Quizzer game;

    private Session player;

    public GameSession(Quizzer game) throws DeploymentException {
        this.game = game;

        Server server = new Server("localhost", 1234, "/", null, WebsocketServer.class);
        server.start();
    }

    public void onOpen(Session session) throws IOException {
        boolean playerNotActive = player == null || !player.isOpen();

        if (playerNotActive) {
            player = session;
        } else {
            closeConnection(session);
            return;
        }

        RemoteEndpoint.Basic remote = session.getBasicRemote();
        remote.sendPing(ByteBuffer.wrap("".getBytes()));
    }

    public void closeConnection(Session session) throws IOException {
        RemoteEndpoint.Basic remote = session.getBasicRemote();
        remote.sendText("Already got player");

        session.close();
    }

    public void onMessage(Session session, String message) throws IOException {
        game.checkIfCorrectAnswer(Integer.parseInt(message));
    }
}
