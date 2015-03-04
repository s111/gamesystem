package com.github.s111.bachelor.pong.network;

import com.github.s111.bachelor.pong.game.Pong;
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
    private final Pong game;

    private Session backend;

    private String player1;
    private String player2;

    public GameSession(Pong game) throws DeploymentException {
        this.game = game;
    }

    public void connect() throws URISyntaxException, IOException, DeploymentException {
        ClientManager client = ClientManager.createClient();
        client.connectToServer(WebsocketClient.class, new URI("ws://localhost:3001/ws"));
    }

    public void onOpen(Session session) throws IOException, EncodeException {
        backend = session;
        player1 = "";
        player2 = "";

        backend.getBasicRemote().sendObject(Json.createObjectBuilder()
                .add("action", "identify")
                .add("data", "game")
                .build());
    }

    public void onMessage(Session session, String message) throws IOException, EncodeException {
        JsonReader jsonReader = Json.createReader(new StringReader(message));
        JsonObject jsonObj = jsonReader.readObject();
        jsonReader.close();

        if (!(jsonObj.containsKey("action") && jsonObj.containsKey("data"))) {
            return;
        }

        String action = jsonObj.getString("action");

        switch (action) {
            case "dropped client": {
                String id = jsonObj.getString("data");

                if (player1.equals(id)) {
                    player1 = "";
                } else if (player2.equals(id)) {
                    player2 = "";
                }

                sendAvailablePaddles("all");

                break;
            }

            case "play as": {
                String id = jsonObj.getString("from");
                String data = jsonObj.getString("data");

                if (data.equals("")) {
                    sendAvailablePaddles(id);

                    break;
                }

                if (player1.equals("") && data.equals("left")) {
                    player1 = id;
                } else if (player2.equals("") && data.equals("right")) {
                    player2 = id;
                }

                sendAvailablePaddles("all");

                break;
            }

            case "move": {
                String data = jsonObj.getJsonNumber("data").toString();

                float position;

                try {
                    position = Float.parseFloat(data);
                } catch (Exception e) {
                    position = -1;
                }

                if (!jsonObj.containsKey("from")) {
                    return;
                }

                String from = jsonObj.getString("from");

                if (player1 != null && player1.equals(from)) {
                    game.movePlayer1(position);
                } else if (player2 != null && player2.equals(from)) {
                    game.movePlayer2(position);
                }

                break;
            }
        }
    }

    private void sendAvailablePaddles(String id) throws IOException, EncodeException {
        backend.getBasicRemote().sendObject(Json.createObjectBuilder()
                .add("action", "passthrough")
                .add("data", Json.createObjectBuilder()
                        .add("action", "play as")
                        .add("data", Json.createObjectBuilder()
                                .add("left", player1)
                                .add("right", player2)))
                .add("to", id)
                .build());
    }
}
