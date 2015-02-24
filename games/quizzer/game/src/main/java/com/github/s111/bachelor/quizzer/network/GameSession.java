package com.github.s111.bachelor.quizzer.network;

import com.github.s111.bachelor.quizzer.game.Quizzer;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class GameSession {
    private final Quizzer game;

    private Player player;

    private Session backend;

    public GameSession(Quizzer game) throws DeploymentException {
        this.game = game;
    }
    public void connect() throws URISyntaxException, IOException, DeploymentException {
        ClientManager client = ClientManager.createClient();
        client.connectToServer(WebsocketClient.class, new URI("ws://localhost:3001/ws"));
    }

    public void onOpen(Session session) throws IOException, EncodeException {
        backend = session;
        backend.getBasicRemote().sendObject(Json.createObjectBuilder()
                .add("action", "identify")
                .add("data", "game")
                .build());
    }

    public void onMessage(Session session, String message) throws IOException {
        JsonReader jsonReader = Json.createReader(new StringReader(message));
        JsonObject jsonObj = jsonReader.readObject();
        jsonReader.close();

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
        int selection;
        try {
            selection = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            selection = -1;
        }

        game.checkIfCorrectAnswer(selection);
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
