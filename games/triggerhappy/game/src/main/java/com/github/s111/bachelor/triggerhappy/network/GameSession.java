package com.github.s111.bachelor.triggerhappy.network;

import com.github.s111.bachelor.triggerhappy.game.Triggerhappy;
import org.glassfish.tyrus.client.ClientManager;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

public class GameSession {
    private final Triggerhappy game;

    private Session backend;

    public GameSession(Triggerhappy game) throws DeploymentException {
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

        String action = jsonObj.getString("action");

        switch (action) {
        }
    }
}
