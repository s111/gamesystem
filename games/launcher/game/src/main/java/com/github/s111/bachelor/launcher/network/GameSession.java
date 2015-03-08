package com.github.s111.bachelor.launcher.network;

import com.github.s111.bachelor.launcher.Application;
import com.github.s111.bachelor.launcher.game.Launcher;
import org.glassfish.tyrus.client.ClientManager;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private final Launcher game;

    private RemoteEndpoint.Basic backend;

    public GameSession(Launcher game) {
        this.game = game;
    }

    public void connect() throws URISyntaxException, IOException, DeploymentException {
        ClientManager client = ClientManager.createClient();
        client.connectToServer(WebsocketClient.class, new URI("ws://localhost:3001/ws"));
    }

    public void onOpen(Session session) throws IOException, EncodeException {
        backend = session.getBasicRemote();
        backend.sendObject(Json.createObjectBuilder()
                .add("action", "identify")
                .add("data", "game")
                .build());
        backend.sendObject(Json.createObjectBuilder()
                .add("action", "list")
                .build());
    }

    public void onMessage(Session session, String message) throws IOException {
        JsonReader jsonReader = Json.createReader(new StringReader(message));
        JsonObject jsonObj = jsonReader.readObject();
        jsonReader.close();

        if (!(jsonObj.containsKey("action") && jsonObj.containsKey("data"))) {
            return;
        }

        String action = jsonObj.getString("action");

        switch (action) {
            case "list": {
                List<String> games = new ArrayList<>();

                for (JsonValue game : jsonObj.getJsonArray("data")) {
                    games.add(game.toString());
                }

                game.setGameList(games);

                break;
            }

            case "start": {
                startGame(jsonObj.getString("data"));

                break;
            }

            case "select": {
                game.setSelectedGame(jsonObj.getString("data"));

                break;
            }
        }
    }

    public void startGame(String name) {
        JsonObject start = Json.createObjectBuilder()
                .add("action", "start")
                .add("data", name)
                .build();

        try {
            backend.sendObject(start);
        } catch (Exception e) {
            Application.fatalError("Could not start game: " + e.getMessage());
        }
    }
}
