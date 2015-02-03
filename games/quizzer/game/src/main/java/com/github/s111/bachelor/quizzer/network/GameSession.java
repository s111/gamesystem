package com.github.s111.bachelor.quizzer.network;

import com.github.s111.bachelor.quizzer.game.Quizzer;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

public class GameSession {
    private final Quizzer game;

    private Session player;

    public GameSession(Quizzer game) throws DeploymentException {
        this.game = game;

        Server server = new Server("localhost", 1234, "/", null, WebsocketServer.class);
        server.start();

        sendReady();
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
        game.checkIfCorrectAnswer(message);
    }

    private void sendReady() {
        ClientManager client = ClientManager.createClient();
        try {
            client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    JsonObject b = Json.createObjectBuilder()
                            .add("action", "ready")
                            .build();

                    session.getAsyncRemote().sendObject(b);
                }
            }, new URI("ws://localhost:3001/ws"));
        } catch (Exception e) {
            System.out.println("Unable to recover; exiting...");
            System.exit(1);
        }
    }
}
